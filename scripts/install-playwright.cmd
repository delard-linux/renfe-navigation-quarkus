@echo off
REM Install Playwright browsers on Windows for this project.
REM - If package.json is missing, it will create one with npm init -y
REM - Then installs @playwright/test as a dev dependency and downloads chromium

echo === Install Playwright helper ===

where node >nul 2>&1
if errorlevel 1 (
  echo Node.js not found on PATH. Please install Node (LTS) and try again.
  exit /b 1
)

where npm >nul 2>&1
if errorlevel 1 (
  echo npm not found on PATH. Ensure you have Node/npm installed.
  exit /b 1
)

where npx >nul 2>&1
if errorlevel 1 (
  echo npx not found on PATH. npm v5+ should provide npx. Please update npm if needed.
  exit /b 1
)

REM Create package.json if not present
if not exist package.json (
  echo package.json not found. Creating minimal package.json (npm init -y)...
  npm init -y
) else (
  echo package.json already exists.
)

echo Installing @playwright/test (as dev dependency)...
npm install --save-dev @playwright/test
if errorlevel 1 (
  echo npm install failed. Check network and npm logs.
  exit /b 1
)

echo Installing Playwright browsers (chromium)...
npx playwright install chromium
if errorlevel 1 (
  echo playwright install failed. Check output above.
  exit /b 1
)

echo
echo Playwright browsers installed in: %USERPROFILE%\AppData\Local\ms-playwright
echo To avoid runtime downloads by Playwright Java, set the following environment variable in your shell before running tests:
echo set PLAYWRIGHT_BROWSERS_PATH=C:\Users\<your-user>\AppData\Local\ms-playwright

echo Done.
exit /b 0

