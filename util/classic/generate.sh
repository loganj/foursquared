for file in ../../captures/types/classic/*; do
  base=$(basename $file .xml);
  base=$(echo -n ${base:0:1}|tr '[:lower:]' '[:upper:]'; echo -n ${base:1});
  echo "./gen_parser.py $file > ../../src/com/joelapenna/foursquare/parsers/classic/${base}Parser.java";
  ./gen_parser.py $file > ../../src/com/joelapenna/foursquare/parsers/classic/${base}Parser.java;
  echo "./gen_class.py $file > ../../src/com/joelapenna/foursquare/types/classic/${base}.java";
  ./gen_class.py $file > ../../src/com/joelapenna/foursquare/types/classic/${base}.java;
done;
