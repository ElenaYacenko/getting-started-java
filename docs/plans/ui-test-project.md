# План развития учебного проекта UI-автотестов

## Назначение

Постепенно добавить TestBase, независимые сценарии, Page Object, Allure-отчёты (по решению фазы 4 — генератор Allure Report 2), GitHub Actions, Telegram и Jenkins, сохраняя существующий Java-стек.

В этом документе хранятся согласованные решения, статусы фаз, результаты проверок, ограничения и открытые вопросы. Общие правила работы агента находятся отдельно в [AGENTS.md](../../AGENTS.md). После каждой фазы обновлять этот план; не отмечать выполнение без подтверждённых результатов.

## Согласованные решения

- Java 21 через Gradle Java toolchain; Gradle Wrapper 9.6.0, bin-дистрибутив.
- Selenide 7.19.0 (Selenium 4.51.0, CDP v153–v155), JUnit Jupiter 6.1.3.
- Defaults: baseUrl=https://github.com, browser=chrome, browserSize=1920x1080, headless=false.
- Переопределение через -DbaseUrl, -Dbrowser, -DbrowserSize, -Dheadless с явной передачей этих свойств из Gradle в JVM тестов.
- В CI явно передавать headless=true.
- Page Object: fluent API; проверки в should…-методах страниц; private final SelenideElement-поля; имена методов описывают пользовательские действия.
- Отдельные компоненты выделять только при реальном повторении блоков. Не добавлять BasePage, фабрики и сложную конфигурационную систему без необходимости.
- Структура Page Object: packages `tests` (SearchTests, TestBase) и `pages` (SearchPage, SearchResultsPage). open("/search") живёт в SearchPage.open() и остаётся относительным к baseUrl; assertions — в should…-методах страниц (shouldHaveResult, shouldBeEmpty) с исходными локаторами и порядком проверок.
- Отчёты: генератор — Allure Report 2 CLI 2.46.1 (локально через Homebrew; в CI — официальный tgz с Maven Central с закреплённым SHA-256); Node.js и npm-зависимости в проект не вводятся.
- JVM-адаптеры: Allure Java 3.0.0 — `allure-jupiter` (JUnit 5 и 6) + `allure-selenide` (Selenide 7.x), версии выравниваются через `allure-bom`; формат результатов совместим с генератором Report 2.
- Шаги в отчёте: `@Step` на методах страниц (требует `-javaagent:aspectjweaver` на JVM тестов, версия 1.9.25) + технические шаги Selenide от `AllureSelenide` (вкладываются в пользовательские). Вложения — Screenshot и Page source только при падении (дефолты `AllureSelenide`); listener регистрируется в `@BeforeAll` TestBase.
- Результаты Allure — `build/allure-results`, отчёт — `build/allure-report`; оба каталога внутри `build/` и не попадают в git.
- GitHub Actions: workflow_dispatch только; runner ubuntu-24.04; Chrome for Testing 153.0.8010.52 + ChromeDriver той же версии; Allure CLI 2.46.1 с SHA-256; Pages publish из default branch при успешной генерации отчёта; concurrency group ui-tests-github-search; все actions pinned по SHA.

## Текущее состояние

- Фаза 0 завершена; исходный запуск проверен.
- Фаза 1 завершена: TestBase с конфигурацией, setUp/tearDown, AllureSelenide logger.
- Фаза 2 завершена: 3 теста (позитивный + 2 негативных).
- Фаза 3 завершена: Page Object (SearchPage, SearchResultsPage) с @Step.
- Фаза 4 завершена: Allure Java 3.0.0 (адаптеры) + Allure Report 2.46.1 CLI (генератор).
- **Фаза 5 реализована (2026-11-26):** `.github/workflows/ui-tests.yml`, `build.gradle` (conditional прокидывание browserBinary/chrome driver). Коммит: pending (после подтверждения).
- Фазы 6–7 отложены в домашнее задание; не реализованы.
- Фаза 8 (Review) — не выполнена.
- Фаза 9 (доработка findings) — не выполнена.

## Фаза 5 — GitHub Actions

Статус: реализована 2026-11-26.

Файлы: `.github/workflows/ui-tests.yml`, `build.gradle`.

### Триггеры и inputs

- Только `workflow_dispatch`. push, pull_request, schedule НЕ используются.
- `browser`: choice, единственное значение — chrome.
- `browserSize`: строка, валидация regex `^([0-9]{3,4}x[0-9]{3,4})$`.
- `baseUrl` зафиксирован: https://github.com (не input).
- `headless=true` всегда.

### Шаги workflow

1. checkout (persist-credentials: false)
2. setup-java (Temurin 21)
3. setup-gradle
4. setup-chrome (Chrome for Testing 153.0.8010.52) + проверка совпадения версий Chrome/ChromeDriver
5. Download Allure CLI 2.46.1 с SHA-256 проверкой + проверка Java 21
6. compile: `./gradlew testClasses` — отдельно от test
7. test: `./gradlew test` с прокидыванием -Dselenide.browserBinary, -Dwebdriver.chrome.driver, headless=true, browserSize
8. upload-artifact: allure-results (если compile успешен, даже при падении тестов)
9. upload-artifact: junit-and-gradle-report
10. генерация Allure-отчёта (без *-result.json — явное падение)
11. deploy-report: upload-pages-artifact + deploy-pages (только default branch, только при успешной генерации отчёта)

### Ключевые решения

- Runner: ubuntu-24.04 (не -latest), timeout-minutes: 30.
- Все actions pinned по SHA (версии не моложе 7 дней на момент выбора).
- Permissions: contents: read на workflow level; pages: write и id-token: write только у job deploy-report.
- Concurrency: одна глобальная группа `ui-tests-github-search`, cancel-in-progress: false (снижение риска HTTP 429).
- Артефакты: retention-days: 14.
- Без continue-on-error, || true, tee. build/ не кэшируется. Каждый запуск — чистый runner.
- deploy-report: publishes Pages только из default branch и только при успешной генерации отчёта (report_generated output из job ui-tests).
- Comment-задел на фазу 9: ужесточение «только при зелёных тестах» — не применено, оставлен комментарий в YAML.

### Ограничения

- Зависимость от живого github.com — данные qa.guru, семантика stars:>999999999, возможные HTTP 429; без ретраев.
- Chrome for Testing на ubuntu-24.04:AppArmor — требует profile для userns (решение из фазы 9, сейчас не применено).
- Локальная проверка через `act` не выполнена (требует Docker).
- Визуальная приёмка Pages-отчёта пользователем не зафиксирована.

## Базовые команды проверки

```bash
./gradlew --version
./gradlew test --rerun-tasks --console=plain
./gradlew test -Dheadless=true -DbrowserSize=1366x768 --rerun-tasks --console=plain
allure --version
allure generate build/allure-results --clean -o build/allure-report
allure open build/allure-report
git diff --check
```

## Критерий завершения фазы

1. Выполнен именно согласованный объём.
2. Проверки действительно запущены и результаты зафиксированы.
3. Временная диагностика удалена, ограничения и блокеры перечислены.
4. Этот план обновлён без превращения предположений в подтверждённые результаты.
5. Свои изменения закоммичены; чужой WIP не затронут.
6. Работа остановлена до подтверждения следующей фазы.
