@echo off
chcp 65001 >nul
rem Cap nhat NhatKyCongViec_PhamTranTuanAnh.docx voi so lieu moi nhat.
rem So lieu duoc script tu dem lai tu ma nguon moi lan chay, khong go tay.
rem Dong tep docx trong Word hoac WPS truoc khi chay, neu khong se khong ghi duoc.

cd /d "%~dp0"

if not exist node_modules\docx (
    echo Chua co thu vien docx, dang cai...
    call npm install docx --no-audit --no-fund
    if errorlevel 1 goto loi
)

node tao-nhat-ky.js "E:\vuonsen-fnb\doc\NhatKyCongViec_PhamTranTuanAnh.docx"
if errorlevel 1 goto loi

echo.
echo Da cap nhat nhat ky. Mo tep de xem:
echo   E:\vuonsen-fnb\doc\NhatKyCongViec_PhamTranTuanAnh.docx
pause
exit /b 0

:loi
echo.
echo Khong cap nhat duoc. Neu bao loi ghi tep thi dong docx trong Word/WPS roi chay lai.
pause
exit /b 1
