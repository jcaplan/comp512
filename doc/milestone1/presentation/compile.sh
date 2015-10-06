#!/bin/bash

mkdir -p figures

for file in *.dot; do

   dot -Tpdf ${file} -o "figures/${file%.*}.pdf"

done


pdflatex presentation_1.tex 
bibtex presentation_1.aux
pdflatex presentation_1.tex 



exit 0
