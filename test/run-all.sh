#!/bin/csh

cd ../build/classes

foreach f (../../examples/eofm-models/test*.xml)
  echo 'Running '$f' ...'
  java eofm/Main -i $f 
  echo 'Checking '$f:r'.sal ...'
  sal-smc -v 3 $f:r.sal >& $f:r.out
  mv $f:r.sal $f:r:s/eofm/sal/.sal
  mv $f:r.out $f:r:s/eofm-models/output/.out
end
