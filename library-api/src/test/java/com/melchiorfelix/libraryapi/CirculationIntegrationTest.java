package com.melchiorfelix.libraryapi;

import com.melchiorfelix.libraryapi.api.dto.CheckoutRequest;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.CopyStatus;
import com.melchiorfelix.libraryapi.model.repository.*;
import com.melchiorfelix.libraryapi.service.LoanService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "library.circulation.max-active-loans=2",
        "library.circulation.max-renewals=1",
        "library.circulation.renewal-days=7"
})
@AutoConfigureMockMvc
class CirculationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired LoanRepository loans;
    @Autowired BookCopyRepository copies;
    @Autowired MemberRepository members;
    @Autowired BookRepository books;
    @Autowired LoanService service;
    @MockitoBean Clock clock;

    @BeforeEach
    void reset() {
        loans.deleteAll();
        copies.deleteAll();
        members.deleteAll();
        books.deleteAll();
        today("2026-09-24");
    }

    @Test
    void completesMemberInventoryCheckoutRenewalAndReturnWorkflow() throws Exception {
        long member = member("CARD-1");
        long book = book("123");
        long copy = copy(book, "COPY-1");
        long loan = checkout("123", member, copy);
        mvc.perform(get("/api/loans/" + loan)).andExpect(status().isOk())
                .andExpect(jsonPath("memberId").value(member))
                .andExpect(jsonPath("copyId").value(copy))
                .andExpect(jsonPath("loanDate").value("2026-09-24"))
                .andExpect(jsonPath("dueDate").value("2026-10-08"))
                .andExpect(jsonPath("returned").value(false));
        mvc.perform(post("/api/loans/" + loan + "/renew")).andExpect(status().isOk())
                .andExpect(jsonPath("dueDate").value("2026-10-15"))
                .andExpect(jsonPath("renewalCount").value(1));
        mvc.perform(post("/api/loans/" + loan + "/renew")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0]").value("Renewal limit reached"));
        mvc.perform(get("/api/books/" + book + "/copies").param("status", "AVAILABLE"))
                .andExpect(jsonPath("totalElements").value(0));
        today("2026-09-25");
        returnLoan(loan);
        mvc.perform(get("/api/loans/" + loan)).andExpect(jsonPath("returnedDate").value("2026-09-25"));
        mvc.perform(get("/api/members/" + member + "/loans"))
                .andExpect(status().isOk()).andExpect(jsonPath("content[0].returned").value(true));
        mvc.perform(get("/api/books/" + book + "/loans"))
                .andExpect(status().isOk()).andExpect(jsonPath("content[0].barcode").value("COPY-1"));
        mvc.perform(get("/api/books/" + book + "/copies").param("status", "AVAILABLE"))
                .andExpect(jsonPath("totalElements").value(1));
        mvc.perform(post("/api/loans/" + loan + "/renew")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0]").value("Returned loans cannot be renewed"));
    }

    @Test
    void twoCopiesCanCirculateIndependentlyAndExhaustInventory() throws Exception {
        long firstMember = member("CARD-1");
        long secondMember = member("CARD-2");
        long book = book("123");
        long firstCopy = copy(book, "COPY-1");
        long secondCopy = copy(book, "COPY-2");
        long firstLoan = checkout("123", firstMember, null);
        long secondLoan = checkout("123", secondMember, null);
        assertThat(loans.findById(firstLoan).orElseThrow().getCopy().getId()).isEqualTo(firstCopy);
        assertThat(loans.findById(secondLoan).orElseThrow().getCopy().getId()).isEqualTo(secondCopy);
        request(post("/api/loans"), new CheckoutRequest("123", firstMember, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0]").value("No available copies for this book"));
        returnLoan(firstLoan);
        long replacement = checkout("123", firstMember, firstCopy);
        returnLoan(firstLoan);
        assertThat(copies.findById(firstCopy).orElseThrow().getStatus()).isEqualTo(CopyStatus.ON_LOAN);
        assertThat(loans.findById(replacement).orElseThrow().getReturned()).isFalse();
    }

    @Test
    void tracksOverdueDatesAndBlocksBorrowingAndRenewalUntilReturn() throws Exception {
        long member = member("CARD-1");
        long book = book("123");
        copy(book, "COPY-1");
        copy(book, "COPY-2");
        long loan = checkout("123", member, null);
        today("2026-10-08");
        mvc.perform(get("/api/loans/overdue")).andExpect(jsonPath("totalElements").value(0));
        today("2026-10-09");
        mvc.perform(get("/api/loans/overdue")).andExpect(status().isOk())
                .andExpect(jsonPath("content[0].id").value(loan));
        mvc.perform(get("/api/loans").param("overdue", "false"))
                .andExpect(jsonPath("totalElements").value(0));
        request(post("/api/loans"), new CheckoutRequest("123", member, null))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("errors[0]").value("Member has overdue loans"));
        mvc.perform(post("/api/loans/" + loan + "/renew")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0]").value("Member has overdue loans"));
        returnLoan(loan);
        mvc.perform(get("/api/loans/overdue")).andExpect(jsonPath("totalElements").value(0));
        checkout("123", member, null);
    }

    @Test
    void allowsRenewalOnDueDate() throws Exception {
        long member = member("CARD-1");
        long book = book("123");
        copy(book, "COPY-1");
        long loan = checkout("123", member, null);
        today("2026-10-08");
        mvc.perform(post("/api/loans/" + loan + "/renew")).andExpect(status().isOk())
                .andExpect(jsonPath("dueDate").value("2026-10-15"));
    }

    @Test
    void suspensionBlocksNewLoansAndRenewalsButAllowsReturns() throws Exception {
        long member = member("CARD-1");
        long book = book("123");
        copy(book, "COPY-1");
        long loan = checkout("123", member, null);
        request(patch("/api/members/" + member), Map.of("active", false)).andExpect(status().isOk());
        request(post("/api/loans"), new CheckoutRequest("123", member, null))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("errors[0]").value("Member is suspended"));
        mvc.perform(post("/api/loans/" + loan + "/renew")).andExpect(status().isBadRequest());
        returnLoan(loan);
        request(patch("/api/members/" + member), Map.of("active", true)).andExpect(status().isOk());
        checkout("123", member, null);
    }

    @Test
    void enforcesConfiguredActiveLoanLimitAndFreesSlotOnReturn() throws Exception {
        long member = member("CARD-1");
        long book = book("123");
        for (int i = 1; i <= 3; i++) copy(book, "COPY-" + i);
        long first = checkout("123", member, null);
        checkout("123", member, null);
        request(post("/api/loans"), new CheckoutRequest("123", member, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0]").value("Member has reached the active loan limit"));
        assertThat(loans.count()).isEqualTo(2);
        assertThat(copies.findByBookIdAndStatus(book, CopyStatus.AVAILABLE,
                org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        returnLoan(first);
        checkout("123", member, null);
    }

    @Test
    void validatesMembersNormalizesCardsAndSupportsProfileSearch() throws Exception {
        request(post("/api/members"), Map.of("cardNumber", " ", "name", "", "email", "invalid"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("errors.length()").value(3));
        long member = member(" card-1 ");
        mvc.perform(get("/api/members/" + member)).andExpect(jsonPath("cardNumber").value("CARD-1"));
        request(post("/api/members"), Map.of("cardNumber", "Card-1", "name", "Other", "email", "other@example.org"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("errors[0]").value("Card number already registered"));
        request(put("/api/members/" + member), Map.of("cardNumber", "CARD-1", "name", "Updated Member",
                "email", "updated@example.org")).andExpect(status().isOk());
        mvc.perform(get("/api/members").param("query", "updated").param("active", "true").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("content[0].name").value("Updated Member"));
        request(patch("/api/members/" + member), Map.of()).andExpect(status().isBadRequest());
    }

    @Test
    void borrowerNameHistorySurvivesProfileChanges() throws Exception {
        long member = member("CARD-1");
        long book = book("123");
        copy(book, "COPY-1");
        long loan = checkout("123", member, null);
        request(put("/api/members/" + member), Map.of("cardNumber", "CARD-1", "name", "New Name",
                "email", "new@example.org")).andExpect(status().isOk());
        mvc.perform(get("/api/loans/" + loan)).andExpect(jsonPath("customer").value("Sam Taylor"));
    }

    @Test
    void uniqueBarcodesAndWithdrawnCopiesProtectInventory() throws Exception {
        long member = member("CARD-1");
        long book = book("123");
        long copy = copy(book, "COPY-1");
        request(post("/api/books/" + book + "/copies"), Map.of("barcode", "COPY-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("errors[0]").value("Barcode already registered"));
        request(post("/api/books/" + book + "/copies"), Map.of("barcode", " "))
                .andExpect(status().isBadRequest());
        long loan = checkout("123", member, copy);
        mvc.perform(post("/api/copies/" + copy + "/withdraw")).andExpect(status().isBadRequest());
        returnLoan(loan);
        mvc.perform(post("/api/copies/" + copy + "/withdraw")).andExpect(status().isOk())
                .andExpect(jsonPath("status").value("WITHDRAWN"));
        request(post("/api/loans"), new CheckoutRequest("123", member, copy))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("errors[0]").value("Copy is not available"));
        request(post("/api/loans"), new CheckoutRequest("123", member, null)).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/books/" + book)).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsCopyFromAnotherBookAndUnknownEntities() throws Exception {
        long member = member("CARD-1");
        long first = book("123");
        long second = book("456");
        long copy = copy(second, "COPY-1");
        request(post("/api/loans"), new CheckoutRequest("123", member, copy))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0]").value("Copy does not belong to the requested book"));
        request(post("/api/loans"), new CheckoutRequest("123", 999999L, null)).andExpect(status().isNotFound());
        request(post("/api/loans"), new CheckoutRequest("missing", member, null)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/members/999999/loans")).andExpect(status().isNotFound());
        mvc.perform(get("/api/books/999999/copies")).andExpect(status().isNotFound());
        mvc.perform(post("/api/loans/999999/renew")).andExpect(status().isNotFound());
        assertThat(loans.count()).isZero();
        assertThat(copies.findById(copy).orElseThrow().getStatus()).isEqualTo(CopyStatus.AVAILABLE);
    }

    @Test
    void loanFiltersCombineAndPaginationIncludesHistory() throws Exception {
        long member = member("CARD-1");
        long book = book("123");
        copy(book, "COPY-1");
        copy(book, "COPY-2");
        long first = checkout("123", member, null);
        checkout("123", member, null);
        returnLoan(first);
        mvc.perform(get("/api/loans").param("memberId", Long.toString(member)).param("returned", "true"))
                .andExpect(jsonPath("totalElements").value(1)).andExpect(jsonPath("content[0].id").value(first));
        mvc.perform(get("/api/loans").param("isbn", "different").param("customer", "Sam Taylor"))
                .andExpect(jsonPath("totalElements").value(0));
        mvc.perform(get("/api/loans").param("size", "1").param("page", "1").param("sort", "id,asc"))
                .andExpect(jsonPath("totalElements").value(2)).andExpect(jsonPath("content.length()").value(1));
    }

    @Test
    void updatesBookFromJsonWithoutChangingIsbnOrLosingInventory() throws Exception {
        long book = book("123");
        copy(book, "COPY-1");
        request(put("/api/books/" + book), Map.of("title", "Updated title", "author", "New author", "isbn", "123"))
                .andExpect(status().isOk()).andExpect(jsonPath("title").value("Updated title"));
        mvc.perform(get("/api/books/" + book)).andExpect(jsonPath("author").value("New author"));
        mvc.perform(get("/api/books/" + book + "/copies")).andExpect(jsonPath("totalElements").value(1));
    }

    @Test
    void concurrentCheckoutsCannotAllocateSameCopyTwice() throws Exception {
        long firstMember = member("CARD-1");
        long secondMember = member("CARD-2");
        long book = book("123");
        copy(book, "COPY-1");
        List<Boolean> outcomes = race(new CheckoutRequest("123", firstMember, null),
                new CheckoutRequest("123", secondMember, null));
        assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        assertThat(loans.count()).isEqualTo(1);
        assertThat(copies.findAll().get(0).getStatus()).isEqualTo(CopyStatus.ON_LOAN);
    }

    @Test
    void concurrentCheckoutsCannotExceedMemberLimitAcrossBooks() throws Exception {
        long member = member("CARD-1");
        long first = book("123");
        long second = book("456");
        long third = book("789");
        copy(first, "COPY-1");
        copy(second, "COPY-2");
        copy(third, "COPY-3");
        checkout("123", member, null);
        assertThat(race(new CheckoutRequest("456", member, null), new CheckoutRequest("789", member, null)))
                .containsExactlyInAnyOrder(true, false);
        assertThat(loans.countByMemberIdAndReturnedFalse(member)).isEqualTo(2);
    }

    private List<Boolean> race(CheckoutRequest first, CheckoutRequest second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (CheckoutRequest request : List.of(first, second)) {
                results.add(pool.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Start timed out");
                    try {
                        service.checkout(request);
                        return true;
                    } catch (BusinessException expected) {
                        return false;
                    }
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            return List.of(results.get(0).get(20, TimeUnit.SECONDS), results.get(1).get(20, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            pool.shutdownNow();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private long member(String card) throws Exception {
        return id(request(post("/api/members"), Map.of("cardNumber", card, "name", "Sam Taylor",
                "email", "sam@example.org")).andExpect(status().isCreated()));
    }

    private long book(String isbn) throws Exception {
        return id(request(post("/api/books"), Map.of("title", "The Adventures", "author", "Alex Smith",
                "isbn", isbn)).andExpect(status().isCreated()));
    }

    private long copy(long book, String barcode) throws Exception {
        return id(request(post("/api/books/" + book + "/copies"), Map.of("barcode", barcode, "shelfLocation", "A-1"))
                .andExpect(status().isCreated()));
    }

    private long checkout(String isbn, long member, Long copy) throws Exception {
        return Long.parseLong(request(post("/api/loans"), new CheckoutRequest(isbn, member, copy))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private void returnLoan(long id) throws Exception {
        request(patch("/api/loans/" + id), Map.of("returned", true)).andExpect(status().isOk());
    }

    private ResultActions request(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    private long id(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString()).get("id").asLong();
    }

    private void today(String date) {
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(LocalDate.parse(date).atTime(12, 0).toInstant(ZoneOffset.UTC));
    }
}
