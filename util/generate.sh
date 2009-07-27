for file in ../captures/types/v1/*; do
  base=$(basename $file .xml);
  base=$(echo -n ${base:0:1}|tr '[:lower:]' '[:upper:]'; echo -n ${base:1});
  echo "./gen_parser.py $file > ../main/src/com/joelapenna/foursquare/parsers/${base}Parser.java";
  ./gen_parser.py $file > ../main/src/com/joelapenna/foursquare/parsers/${base}Parser.java;
  echo "./gen_class.py $file > ../main/src/com/joelapenna/foursquare/types/${base}.java";
  ./gen_class.py $file > ../main/src/com/joelapenna/foursquare/types/${base}.java;
done;
