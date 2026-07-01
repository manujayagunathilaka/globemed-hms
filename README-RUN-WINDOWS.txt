Globe Med Healthcare Management System - v1.0.0 (Windows Launcher Package)
==========================================================================

This package contains the executable files and configuration files required to run the Globe Med Healthcare Management System on Windows.

Requirements:
-------------
- Windows 10/11 (64-bit/32-bit)
- Java Runtime Environment (JRE) or Java Development Kit (JDK) 17 or later installed.
  You can download Java from Adoptium: https://adoptium.net/temurin/releases/?version=17

How to Run:
-----------
1. Install Java 17 or later on your Windows system.
2. Extract the contents of this zip file if you haven't already.
3. In the extracted folder, copy '.env.example' and rename it to '.env'.
4. Open the '.env' file in a text editor and enter your local MySQL database credentials:
   - DB_URL: The connection URL (default: jdbc:mysql://localhost:3306/globemed-hms)
   - DB_USER: Your MySQL username (e.g., root)
   - DB_PASSWORD: Your MySQL password
5. Set up your local database:
   - Make sure your MySQL Server is running.
   - Run the database/schema.sql file to create the database structure.
   - Run the database/seed-demo-data.sql file to seed initial test accounts and data.
6. Double-click the 'globemed-hms-v1.0.0-windows.exe' file to launch the application.

Important Notes:
----------------
- Do not move 'globemed-hms-v1.0.0-windows.exe' out of this folder, as it requires the 'globemed-hms-v1.0.0.jar' file to remain in the same directory.
- This is a Windows executable launcher package, not a full system installer.
- Keep the '.env' file secure and never commit it if you push changes to GitHub.
