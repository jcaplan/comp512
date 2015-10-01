#!/bin/bash

for file in *.dot; do

   dot -Tpdf ${file} -o "${file%.*}.pdf"

done


pdflatex milestone1.tex 

pdflatex milestone1.tex 



exit 0
