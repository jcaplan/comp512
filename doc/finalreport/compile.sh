#!/usr/bin/env bash

for file in *.dot; do

   dot -Tpdf ${file} -o "${file%.*}.pdf"

done


pdflatex finalreport.tex 

pdflatex finalreport.tex 



exit 0
