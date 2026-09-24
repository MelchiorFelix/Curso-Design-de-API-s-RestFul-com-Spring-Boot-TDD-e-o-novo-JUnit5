package com.melchiorfelix.libraryapi.api.dto;

import com.melchiorfelix.libraryapi.model.entity.BookCopy;
import com.melchiorfelix.libraryapi.model.entity.CopyStatus;

public record CopyDTO(Long id, Long bookId, String barcode, String shelfLocation, CopyStatus status) {
    public static CopyDTO from(BookCopy copy) {
        return new CopyDTO(copy.getId(), copy.getBook().getId(), copy.getBarcode(),
                copy.getShelfLocation(), copy.getStatus());
    }
}
