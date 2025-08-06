# Claude.md - Especificações do Projeto

## Visão Geral do Projeto

### Nome da Aplicação
**MyFinances**

### Descrição
Aplicação completa para gestão financeira pessoal que permite acompanhar receitas, despesas, categorizar gastos e monitorizar investimentos em stocks, ETFs e contas poupança. O objetivo é fornecer uma visão clara e detalhada da situação financeira pessoal, com relatórios e análises para ajudar na tomada de decisões financeiras.

### Público-Alvo
- Pessoas que querem controlar melhor as suas finanças pessoais
- Investidores iniciantes e intermédios
- Utilizadores que procuram uma alternativa simples a aplicações como Mint, YNAB ou Toshl
- Pessoas que valorizam privacidade e controlo sobre os seus dados financeiros

## Plataformas Suportadas

- [x] **Web** (Browser - foco principal)
- [ ] **Mobile** (Android - futuro)

## Stack Tecnológica

### Backend + Frontend Web (Atual)
- **Java 17** (LTS)
- **Spring Boot 3.2+**
- **Spring Security 6** (autenticação web + JWT para mobile futuro)
- **Spring Data JPA** (acesso a dados)
- **Spring Web MVC** (controllers web + REST APIs)
- **Thymeleaf** (template engine)
- **Bootstrap 5** (CSS framework)
- **Chart.js** (gráficos financeiros)
- **HTMX** (interatividade dinâmica)
- **Alpine.js** (JavaScript reativo leve)

### Mobile Android (Futuro)
- **Kotlin** (linguagem principal)
- **Jetpack Compose** (UI moderna)
- **Retrofit** (cliente HTTP para APIs REST)
- **Room** (cache local/offline)
- **Hilt** (dependency injection)
- **Navigation Component** (navegação)
- **Coil** (image loading)

### Base de Dados (Partilhada)
- **PostgreSQL 15+** (produção)
- **H2** (desenvolvimento/testes)
- **Flyway** (migrações de schema)

## Funcionalidades Principais

### 1. Gestão de Receitas e Despesas
- **Adicionar transações** (receitas e despesas)
- **Categorização automática e manual** de transações
- **Contas múltiplas** (conta à ordem, carteira, cartões de crédito)
- **Transferências entre contas**
- **Transações recorrentes** (salário, renda, subscrições)
- **Upload de comprovantes/faturas** (fotos/PDFs)

### 2. Categorização e Análise de Gastos
- **Categorias predefinidas** (Alimentação, Transporte, Saúde, Entretenimento, etc.)
- **Subcategorias personalizáveis**
- **Tags personalizadas** para melhor organização
- **Análise de gastos por categoria**
- **Comparação mensal/anual** de despesas
- **Identificação de padrões de gastos**

### 3. Gestão de Investimentos
- **Portfolio de investimentos**
    - Stocks individuais
    - ETFs
    - Fundos de investimento
    - Contas poupança e depósitos a prazo
- **Tracking de performance**
- **Valor atual vs valor investido**
- **Dividendos e juros recebidos**
- **Diversificação por setor/geografia**

### 4. Relatórios e Dashboard
- **Dashboard principal** com visão geral financeira
- **Gráficos interativos** (receitas vs despesas, evolução património)
- **Relatórios mensais/anuais**
- **Net worth tracking** (património líquido)
- **Cash flow analysis**
- **Projeções financeiras simples**

### 5. Orçamentos e Metas
- **Criação de orçamentos** por categoria
- **Alertas de limite de orçamento**
- **Metas de poupança**
- **Tracking de progresso das metas**

### Funcionalidades Avançadas (Futuras)
- [x] **Export de dados** (PDF, Excel, CSV)
- [x] **Multi-moeda** (EUR, USD, GBP, etc.)
- [x] **Calculadoras financeiras** (compound interest, mortgage)
- [x] **Modo família** (gestão de finanças partilhadas)

## Arquitetura da Aplicação

### Estrutura de Pastas Sugerida
```
money-manager-web/              # Spring Boot Web + API Application
├── src/main/java/
│   └── com/empresa/moneymanager/
│       ├── MoneyManagerApplication.java
│       ├── controller/
│       │   ├── web/            # Controllers para Thymeleaf (Web)
│       │   │   ├── HomeController.java
│       │   │   ├── AuthController.java
│       │   │   ├── DashboardController.java
│       │   │   ├── TransactionController.java
│       │   │   ├── InvestmentController.java
│       │   │   ├── BudgetController.java
│       │   │   └── ReportController.java
│       │   └── api/            # REST Controllers (Android futuro)
│       │       ├── AuthApiController.java
│       │       ├── TransactionApiController.java
│       │       ├── InvestmentApiController.java
│       │       ├── BudgetApiController.java
│       │       └── ReportApiController.java
│       ├── service/
│       │   ├── UserService.java
│       │   ├── TransactionService.java
│       │   ├── InvestmentService.java
│       │   ├── CategoryService.java
│       │   ├── BudgetService.java
│       │   └── ReportService.java
│       ├── repository/
│       │   ├── UserRepository.java
│       │   ├── AccountRepository.java
│       │   ├── TransactionRepository.java
│       │   ├── InvestmentRepository.java
│       │   ├── CategoryRepository.java
│       │   └── BudgetRepository.java
│       ├── model/
│       │   ├── User.java
│       │   ├── Account.java
│       │   ├── Transaction.java
│       │   ├── Category.java
│       │   ├── Investment.java
│       │   ├── Budget.java
│       │   └── Goal.java
│       ├── dto/
│       │   ├── request/         # DTOs para requests (Android)
│       │   │   ├── LoginRequest.java
│       │   │   ├── TransactionRequest.java
│       │   │   └── InvestmentRequest.java
│       │   └── response/        # DTOs para responses (Android)
│       │       ├── UserResponse.java
│       │       ├── TransactionResponse.java
│       │       ├── InvestmentResponse.java
│       │       └── DashboardResponse.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── WebConfig.java
│       │   ├── JwtConfig.java          # Configuração JWT (Android)
│       │   └── DatabaseConfig.java
│       └── security/
│           ├── CustomUserDetailsService.java
│           ├── JwtAuthenticationEntryPoint.java  # JWT (Android)
│           ├── JwtAuthenticationFilter.java      # JWT (Android)
│           ├── JwtTokenProvider.java             # JWT (Android)
│           └── SecurityUtils.java
├── src/main/resources/
│   ├── templates/              # Templates Thymeleaf (Web apenas)
│   │   ├── layout/
│   │   │   ├── main.html
│   │   │   └── auth.html
│   │   ├── fragments/
│   │   │   ├── head.html
│   │   │   ├── header.html
│   │   │   ├── sidebar.html
│   │   │   ├── footer.html
│   │   │   └── modals.html
│   │   └── pages/
│   │       ├── index.html
│   │       ├── auth/
│   │       │   ├── login.html
│   │       │   └── register.html
│   │       ├── dashboard.html
│   │       ├── transactions/
│   │       │   ├── list.html
│   │       │   ├── add.html
│   │       │   └── edit.html
│   │       ├── investments/
│   │       │   ├── portfolio.html
│   │       │   ├── add.html
│   │       │   └── details.html
│   │       ├── budgets/
│   │       │   ├── list.html
│   │       │   └── add.html
│   │       └── reports/
│   │           ├── overview.html
│   │           ├── monthly.html
│   │           └── yearly.html
│   ├── static/
│   │   ├── css/
│   │   │   ├── main.css
│   │   │   ├── dashboard.css
│   │   │   └── charts.css
│   │   ├── js/
│   │   │   ├── main.js
│   │   │   ├── dashboard.js
│   │   │   ├── charts.js
│   │   │   ├── transactions.js
│   │   │   └── htmx-config.js
│   │   ├── images/
│   │   │   ├── logo.png
│   │   │   └── icons/
│   │   └── uploads/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
│       ├── V1__Create_users_table.sql
│       ├── V2__Create_accounts_table.sql
│       ├── V3__Create_categories_table.sql
│       ├── V4__Create_transactions_table.sql
│       ├── V5__Create_investments_table.sql
│       └── V6__Create_budgets_table.sql
├── android-app/                # Projeto Android (Futuro)
│   ├── app/
│   │   ├── src/main/java/
│   │   │   └── com/empresa/moneymanager/
│   │   │       ├── MainActivity.kt
│   │   │       ├── ui/
│   │   │       │   ├── dashboard/
│   │   │       │   ├── transactions/
│   │   │       │   ├── investments/
│   │   │       │   ├── budgets/
│   │   │       │   └── auth/
│   │   │       ├── data/
│   │   │       │   ├── repository/
│   │   │       │   ├── remote/      # Retrofit API clients
│   │   │       │   └── local/       # Room database
│   │   │       ├── domain/
│   │   │       │   ├── model/
│   │   │       │   ├── repository/
│   │   │       │   └── usecase/
│   │   │       ├── di/             # Hilt modules
│   │   │       └── network/        # API interfaces
│   │   ├── src/main/res/
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── src/test/
├── pom.xml
├── README.md
└── docs/
    ├── api-documentation.md        # REST API docs (Android)
    ├── database-schema.md
    ├── web-user-manual.md
    └── deployment-guide.md
```

### Padrões de Design
- **Backend**: MVC Pattern com Spring Boot
    - **Web Controllers** (Thymeleaf views)
    - **API Controllers** (JSON responses para Android)
    - **Services** (Business Logic partilhada)
    - **Repositories** (Data Access partilhado)
- **Dual Authentication**:
    - **Web**: Spring Security com sessões
    - **Mobile**: JWT tokens (futuro)
- **Templates**: Thymeleaf com layouts (Web apenas)
- **Database**: JPA/Hibernate com Flyway migrations (partilhado)
- **Frontend**:
    - **Web**: Server-side rendering + HTMX
    - **Android**: REST API consumption (futuro)
- **Dependency Injection**: Spring Boot native DI

## Interface do Utilizador

### Design System
- **Paleta de Cores**
    - Primária: #2563eb (azul profissional)
    - Secundária: #059669 (verde para receitas/ganhos)
    - Accent: #dc2626 (vermelho para despesas/perdas)
    - Background: #f8fafc (cinza claro)
    - Text: #1e293b (cinza escuro)
    - Success: #10b981 (verde)
    - Warning: #f59e0b (laranja)

- **Tipografia**
    - Fonte principal: Inter ou System Font
    - H1: 2rem (Dashboard titles)
    - H2: 1.5rem (Section headers)
    - H3: 1.25rem (Card titles)
    - Body: 1rem (Regular text)
    - Small: 0.875rem (Labels, captions)

- **Componentes Base**
    - Cards para resumos financeiros
    - Tabelas para transações
    - Gráficos (Chart.js ou similar)
    - Formulários de transações
    - Modais para detalhes
    - Loading states
    - Estados vazios (empty states)

### Wireframes/Mockups
[Links para designs no Figma, Adobe XD, ou sketches]

## Funcionalidades Específicas por Plataforma

### Web (Thymeleaf + Tailwind CSS) - Atual
- **Dashboard financeiro responsivo** com cards de resumo
- **Formulários intuitivos** para adicionar transações/investimentos
- **Tabelas paginadas** com filtros e ordenação
- **Gráficos interativos** (Chart.js) para análise visual
- **Upload de comprovantes** com drag & drop
- **Relatórios em PDF** com iText ou similar
- **Export de dados** (CSV, Excel)
- **Calculadoras financeiras** integradas
- **Tema escuro/claro** (opcional)
- **Notificações toast** para feedback do utilizador

### Android (Jetpack Compose) - Futuro
- **Interface Material Design 3** nativa
- **Dashboard com cards** e gráficos otimizados para mobile
- **Adicionar transações rapidamente** (floating action button)
- **Scanner de faturas** com câmara (OCR futuro)
- **Notificações push** para lembretes de orçamento
- **Widgets na home screen** com saldo atual
- **Sincronização offline** com Room database
- **Biometrics** para login (fingerprint/face)
- **Partilha rápida** de relatórios
- **GPS tagging** para localização de gastos (opcional)

## Configuração de Desenvolvimento

### Pré-requisitos
- **Java 17** (OpenJDK ou Oracle JDK)
- **Maven 3.8+** (gestão de dependências)
- **PostgreSQL 15+** (base de dados)
- **IDE**: IntelliJ IDEA, Eclipse, ou VS Code com Extension Pack for Java

### Aplicação Web (Spring Boot)
```bash
# Clonar repositório
git clone [url-do-repositorio]
cd money-manager-web

# Configurar base de dados PostgreSQL
createdb moneymanager

# Executar em desenvolvimento (profile dev)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Ou usando o JAR
./mvnw clean package
java -jar target/money-manager-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Executar testes
./mvnw test

# Acessar aplicação
# http://localhost:8080
```

### Comandos Úteis Maven
```bash
# Limpar e compilar
./mvnw clean compile

# Executar apenas testes unitários
./mvnw test

# Executar testes de integração
./mvnw verify

# Gerar relatório de cobertura
./mvnw jacoco:report

# Build para produção
./mvnw clean package -Pprod
```

### Variáveis de Ambiente

#### Configurações Spring Boot (application.yml)
```yaml
# Perfil padrão (desenvolvimento)
spring:
  application:
    name: money-manager
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/moneymanager}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: ${DDL_AUTO:validate}
    show-sql: ${SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  thymeleaf:
    cache: false
    encoding: UTF-8
    prefix: classpath:/templates/
    suffix: .html
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: ${SERVER_PORT:8080}
  servlet:
    session:
      timeout: 30m

# Configurações específicas da aplicação
app:
  name: MoneyManager
  version: 1.0.0
  upload:
    dir: ${UPLOAD_DIR:./uploads}
  security:
    remember-me-key: ${REMEMBER_ME_KEY:mySecretKey}
    jwt:
      secret: ${JWT_SECRET:myJwtSecretKey}  # Para Android futuro
      expiration: ${JWT_EXPIRATION:86400000} # 24 horas

# Logging
logging:
  level:
    com.empresa.moneymanager: ${LOG_LEVEL:INFO}
    org.springframework.security: ${SECURITY_LOG_LEVEL:WARN}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

---
# Perfil de desenvolvimento
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

logging:
  level:
    com.empresa.moneymanager: DEBUG
    org.springframework.web: DEBUG

---
# Perfil de produção
spring:
  config:
    activate:
      on-profile: prod
  thymeleaf:
    cache: true
  jpa:
    show-sql: false
  flyway:
    enabled: true

logging:
  level:
    com.empresa.moneymanager: WARN
    root: WARN

# CORS Configuration para Android (futuro)
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
```

## Testes

### Spring Boot Web Application
- **Unit Tests**: JUnit 5 + Mockito + AssertJ
- **Integration Tests**: @SpringBootTest + TestContainers (PostgreSQL)
- **Web Layer Tests**: @WebMvcTest + MockMvc
- **Repository Tests**: @DataJpaTest + TestEntityManager
- **Security Tests**: @WithMockUser + Spring Security Test

### Cobertura e Qualidade
- **Coverage**: JaCoCo (mínimo 80%)
- **Static Analysis**: SpotBugs + Checkstyle
- **Testes E2E**: Selenium WebDriver (fluxos críticos)

## Deploy e Distribuição

### Aplicação Web (Spring Boot)
- **Desenvolvimento**: Localhost com H2 ou PostgreSQL local
- **Staging**: Environment de testes com PostgreSQL
- **Produção**:
    - **Cloud Providers**:
        - Heroku (simples, PostgreSQL incluído)
        - DigitalOcean App Platform
        - AWS (EC2 + RDS PostgreSQL)
        - Google Cloud Run
    - **VPS Tradicional**:
        - Ubuntu/CentOS com nginx reverse proxy
        - Docker deployment
    - **CI/CD**: GitHub Actions, GitLab CI, ou Jenkins

### Containerização (Docker)
```dockerfile
# Dockerfile exemplo
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/money-manager-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Estrutura de Deploy
- **Nginx**: Reverse proxy + static files
- **SSL**: Let's Encrypt certificates
- **Monitoring**: Actuator endpoints + Micrometer
- **Backup**: Automated PostgreSQL backups

## Requisitos de Performance

### Métricas Alvo
- **Tempo de carregamento inicial**: < 3s
- **First Contentful Paint**: < 1.5s
- **Time to Interactive**: < 5s
- **Uso de memória**: < 100MB (mobile), < 200MB (desktop)

### Otimizações
- Code splitting
- Lazy loading
- Caching strategies
- Image optimization

## Segurança

### Medidas de Segurança
- [ ] Autenticação segura
- [ ] Validação de inputs
- [ ] Proteção contra XSS
- [ ] Dados sensíveis encriptados
- [ ] Comunicação HTTPS
- [ ] Compliance com GDPR (se aplicável)

## Roadmap de Desenvolvimento

### Fase 1 (MVP Web) - 2-3 meses
- [ ] Setup inicial: Spring Boot + PostgreSQL + Thymeleaf
- [ ] Sistema de autenticação web (registo, login, logout)
- [ ] Modelo de dados base (User, Account, Transaction, Category)
- [ ] CRUD de contas financeiras
- [ ] CRUD de transações (receitas/despesas)
- [ ] Sistema básico de categorias (predefinidas + custom)
- [ ] Dashboard simples com saldos e resumos
- [ ] Interface responsive com Bootstrap
- [ ] **Preparação para APIs**: Estrutura dual controllers (web + api)
- [ ] Testes unitários básicos

### Fase 2 (Features Core Web) - 2-3 meses
- [ ] Sistema de orçamentos por categoria
- [ ] Gestão de investimentos (stocks, ETFs, poupanças)
- [ ] Upload e gestão de comprovantes
- [ ] Filtros e pesquisa avançada de transações
- [ ] Gráficos básicos com Chart.js
- [ ] Relatórios mensais/anuais em PDF
- [ ] Transações recorrentes
- [ ] **Implementação de APIs REST** para todos os endpoints
- [ ] **JWT Authentication** configurado
- [ ] Melhorias na UI/UX web

### Fase 3 (Web Completo + Preparação Mobile) - 1-2 meses
- [ ] Dashboard avançado com métricas detalhadas
- [ ] Análise de cash flow e net worth
- [ ] Comparação de períodos (MoM, YoY)
- [ ] Export de dados (CSV, Excel)
- [ ] Metas de poupança e tracking
- [ ] **Documentação completa das APIs REST**
- [ ] **CORS configurado** para futuras apps mobile
- [ ] Performance optimization
- [ ] Deploy para produção

### Fase 4 (Android App) - 3-4 meses (Futuro)
- [ ] Setup projeto Android com Kotlin + Jetpack Compose
- [ ] Integração com APIs REST existentes
- [ ] Autenticação JWT no Android
- [ ] UI nativa com Material Design 3
- [ ] Sincronização offline com Room
- [ ] Funcionalidades específicas mobile (câmara, GPS, notificações)
- [ ] Testes Android (Unit, UI, Integration)
- [ ] Deploy para Google Play Store

## Modelo de Dados Principal

### Entidades Core
```sql
-- Utilizadores
User (id, email, password, name, created_at)

-- Contas financeiras (conta à ordem, carteira, cartão crédito)
Account (id, user_id, name, type, balance, currency, created_at)

-- Categorias de transações
Category (id, user_id, name, type, color, icon, parent_id)

-- Transações
Transaction (id, account_id, category_id, amount, description, date, type, receipt_url)

-- Investimentos
Investment (id, user_id, symbol, name, type, quantity, purchase_price, current_price, purchase_date)

-- Orçamentos
Budget (id, user_id, category_id, amount, period, start_date, end_date)

-- Metas de poupança
Goal (id, user_id, name, target_amount, current_amount, target_date, created_at)
```

### Categorias Predefinidas
- **Receitas**: Salário, Freelance, Investimentos, Outros
- **Despesas**:
    - Essenciais: Habitação, Alimentação, Transportes, Saúde, Seguros
    - Não Essenciais: Entretenimento, Restaurantes, Compras, Viagens
    - Investimentos: Stocks, ETFs, Fundos, Poupanças

### Acessibilidade
- [ ] Suporte a screen readers
- [ ] Navegação por teclado
- [ ] Alto contraste
- [ ] Tamanhos de fonte ajustáveis

### Internacionalização
- [ ] Suporte multi-idioma
- [ ] Formatação de datas/números regional
- [ ] RTL support (se necessário)

### Recursos e Links Úteis

#### Documentação Spring Boot
- [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Thymeleaf Documentation](https://www.thymeleaf.org/documentation.html)
- [Spring Data JPA Guide](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

#### Frontend Resources
- [Bootstrap 5 Documentation](https://getbootstrap.com/docs/5.3/getting-started/introduction/)
- [Chart.js Documentation](https://www.chartjs.org/docs/latest/)
- [HTMX Documentation](https://htmx.org/docs/)
- [Alpine.js Documentation](https://alpinejs.dev/start-here)

#### Development Tools
- **IDE**: IntelliJ IDEA Community/Ultimate
- **Extensions recomendadas**:
    - Spring Boot Tools
    - Thymeleaf syntax highlighting
    - Database tools (PostgreSQL)
- **Browser DevTools**:
    - Spring Boot DevTools para hot reload
    - LiveReload para CSS/JS changes

#### Testing & Quality
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)

## Dependências Maven (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>
    
    <groupId>com.empresa</groupId>
    <artifactId>money-manager</artifactId>
    <version>1.0.0</version>
    <name>MoneyManager</name>
    <description>Personal Finance Management Application</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- Development Tools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <!-- Frontend Dependencies (WebJars) -->
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>bootstrap</artifactId>
            <version>5.3.2</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>chartjs</artifactId>
            <version>4.4.0</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>htmx.org</artifactId>
            <version>1.9.6</version>
        </dependency>
        
        <!-- JWT Dependencies (Para Android futuro) -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.8</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```