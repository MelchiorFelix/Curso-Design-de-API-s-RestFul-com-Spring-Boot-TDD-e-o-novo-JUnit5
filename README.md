# Curso-Design-de-API-s-RestFul-com-Spring-Boot-TDD-e-o-novo-JUnit5

## Executar e verificar

Requisitos: JDK 17 ou superior e `JAVA_HOME` configurado. O Maven Wrapper usa
Maven 3.9.16 com checksum SHA-256. Os três módulos usam o gerenciamento de
dependências do Spring Boot 4.1.1; os testes continuam usando a API Jupiter,
agora com o JUnit gerenciado pelo Spring Boot (JUnit 6).

Na raiz do repositório:

```powershell
.\mvnw.cmd -B -ntp clean verify
```

No Linux/macOS, use `./mvnw`. Para executar uma aplicação:

```powershell
.\mvnw.cmd -pl library-api spring-boot:run
.\mvnw.cmd -pl primeiro-projeto-rest spring-boot:run
```

Execute apenas uma por vez na porta padrão 8080. São exemplos didáticos, sem
autenticação ou autorização implementadas; a atualização de dependências não
transforma esses exemplos em um serviço pronto para exposição pública.

Para consultar vulnerabilidades conhecidas nas dependências resolvidas, incluindo
dependências transitivas e de teste (PowerShell 7):

```powershell
.\mvnw.cmd -B -ntp org.apache.maven.plugins:maven-dependency-plugin:3.11.0:tree -DoutputFile=target/dependencies.json -DoutputType=json
pwsh -File scripts/Test-Dependencies.ps1
```

O script consulta o OSV enviando nomes e versões dos pacotes Maven públicos,
salva o resultado em `target/osv-dependencies.json` e falha quando encontra
vulnerabilidades ou não consegue concluir a consulta. Ele não analisa código-fonte,
plugins de build ou componentes embutidos dentro de outros JARs.
O GitHub Actions verifica os três módulos com Java 17 e 21 e consulta o OSV em
pushes, pull requests e semanalmente. O Dependabot acompanha Maven e Actions.

`main` é a branch padrão; use `development` para desenvolvimento.

## Conteúdo original do curso

Descrição
Nesse curso, feito de desenvolvedor para desenvolvedor, trago conceitos fundamentais sobre testes automatizados e Api's RestFul,  além de boas práticas e Clean Code, para, em integração, criarmos API's RestFul utilizando a técnica do TDD (Test Driven Development), onde desenvolveremos orientado a testes, uma API RestFul utilizando Spring Boot. Curso 100% prático onde eu codifico todos os códigos.



Dentre outros conhecimentos, você aprenderá:



A modelar uma API RestFUL, utilizar os métodos HTTP, códigos de resposta, etc.;

Criar serviço de agendamento de tarefas e envio de emails com o Java;

Documentação de API's com Swagger

Implementação de testes automatizados;

Configuração de Deploy Contínuo;

Publicação da API na nuvem.

Para quem é este curso:
Quem deseja aprender técnicas avançadas de desenvolvimento de software
Quem deseja aprender Spring Boot na prática
Interessados em se atualizar no mercado com tecnologia robusta e amplamente utilizada
Quem deseja conhecer conceitos de arquitetura RESTful
Quem deseja aprender a desenvolver com a técnica TDD
