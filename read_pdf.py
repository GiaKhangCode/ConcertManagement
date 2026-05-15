import PyPDF2

with open('seminar concurrency.pdf', 'rb') as file:
    reader = PyPDF2.PdfReader(file)
    with open('extracted_pdf.txt', 'w', encoding='utf-8') as out_file:
        for page in reader.pages:
            out_file.write(page.extract_text() + '\n\n')
