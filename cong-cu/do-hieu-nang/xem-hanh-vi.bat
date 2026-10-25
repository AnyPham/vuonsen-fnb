@echo off
REM Xem bang ghi nhan hanh vi duyet cua khach, dung de chieu luc demo.
REM
REM Phan "quet hanh vi" khong nhin man hinh la thay duoc, vi no ghi ngam o phia may chu.
REM Lenh nay mo thang bang luot_xem ra cho hoi dong nhin so dong tang len that.
REM
REM Chay: cong-cu\do-hieu-nang\xem-hanh-vi.bat

chcp 65001 >nul
echo.
echo ========== BANG luot_xem - GHI NHAN HANH VI DUYET ==========
echo.

"C:\xampp\mysql\bin\mysql.exe" -u root vuonsen_fnb --default-character-set=utf8mb4 -e ^
"SELECT x.id AS STT, IFNULL(u.email,'(chua dang nhap)') AS Khach, s.name AS 'Khong gian da xem', x.xem_luc AS 'Luc xem' FROM luot_xem x LEFT JOIN users u ON x.user_id=u.id LEFT JOIN spaces s ON x.doi_tuong_id=s.id ORDER BY x.xem_luc DESC LIMIT 20;"

echo.
echo ========== TONG HOP: AI XEM GI MAY LAN ==========
echo.

"C:\xampp\mysql\bin\mysql.exe" -u root vuonsen_fnb --default-character-set=utf8mb4 -e ^
"SELECT IFNULL(u.email,'(chua dang nhap)') AS Khach, s.name AS 'Khong gian', COUNT(*) AS 'So lan xem' FROM luot_xem x LEFT JOIN users u ON x.user_id=u.id LEFT JOIN spaces s ON x.doi_tuong_id=s.id GROUP BY x.user_id, x.doi_tuong_id ORDER BY COUNT(*) DESC;"

echo.
pause
