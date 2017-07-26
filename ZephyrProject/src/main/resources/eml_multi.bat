for /f  "delims=|" %%f in ('dir /b /ad /s %1') do java -jar -Xmx1024m -Xms1024m  eml.jar "%%f"
