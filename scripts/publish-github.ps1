param(
    [string]$RepoName = "fintracker",
    [ValidateSet("public", "private")]
    [string]$Visibility = "public"
)

$ErrorActionPreference = "Stop"
$git = "C:\Program Files\Git\cmd\git.exe"
if (-not (Test-Path $git)) { $git = "git" }

Push-Location $PSScriptRoot\..
try {
    if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
        Write-Host "GitHub CLI не найден. Установите: winget install --id GitHub.cli -e" -ForegroundColor Yellow
        Write-Host "Либо создайте репозиторий на github.com и выполните:" -ForegroundColor Yellow
        Write-Host "  git remote add origin https://github.com/<login>/$RepoName.git"
        Write-Host "  git push -u origin main"
        exit 1
    }

    gh auth status
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Требуется вход в GitHub: gh auth login" -ForegroundColor Yellow
        exit 1
    }

    & $git branch -M main
    $existing = & $git remote
    if ($existing -notcontains "origin") {
        gh repo create $RepoName --$Visibility --source . --remote origin --push
    } else {
        & $git push -u origin main
    }
    Write-Host "Готово: https://github.com/$((gh api user --jq .login))/$RepoName" -ForegroundColor Green
}
finally {
    Pop-Location
}
