# Публикация проекта на GitHub

Репозиторий уже инициализирован локально и содержит первый коммит. Осталось создать удалённый репозиторий и выполнить `push`.

## Вариант 1 — через GitHub CLI (рекомендуется)

```powershell
# один раз: установка и вход в аккаунт
winget install --id GitHub.cli -e
gh auth login

# из папки проекта
cd W:\DSH\fintracker
gh repo create fintracker --public --source . --remote origin --push
```

Если репозиторий уже создан на github.com:

```powershell
git remote add origin https://github.com/<ЛОГИН>/fintracker.git
git push -u origin main
```

## Вариант 2 — вручную

1. Создайте пустой репозиторий `fintracker` на https://github.com/new (без README и .gitignore).
2. Выполните:

```powershell
cd W:\DSH\fintracker
git remote add origin https://github.com/<ЛОГИН>/fintracker.git
git branch -M main
git push -u origin main
```

При первом `push` Git Credential Manager откроет окно входа в GitHub — это нормально.

## Что попадёт в репозиторий

- исходный код приложения (`app/src`);
- Gradle-обёртка и версии зависимостей;
- документация (`docs/`), презентация (`presentation/`);
- CI-workflow `.github/workflows/android.yml`;
- схема Room (`app/schemas`).

APK лежит в `dist/FinTracker-debug.apk` и включён в репозиторий, чтобы проект можно было
проверить без сборки. Локальные файлы (`local.properties`, `.gradle`, `build/`) не входят;
APK также можно пересобрать командой `./gradlew assembleDebug` или скачать из артефактов GitHub Actions.
