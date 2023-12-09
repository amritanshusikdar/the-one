'''
This script will turn an xml file exported from draw.io containing lines and polygons
into a representation in the "Well-known text representation of geometry" (WKT) format.
It will read the file path to the xml file as it's only argument and output the file map.wkt.
The script will simply overwrite map.wkt, should it already exist.
'''

import xml.etree.ElementTree as ET
import sys

def parse_xml_to_wkt(xml_file_path):
    tree = ET.parse(xml_file_path)
    root = tree.getroot()

    # As the map contains multiple seperate lines, these lines will be save in a multilinestring
    multilinestrings = []
    polygons = []

    for mxCell in root.iter('mxCell'):
        geometry = mxCell.find('mxGeometry')
        if geometry is not None:
            # Geometry might contain two points that make up a line and additionally an array of points that make up a polygon
            source_point = geometry.find('mxPoint[@as="sourcePoint"]')
            target_point = geometry.find('mxPoint[@as="targetPoint"]')
            points_array = geometry.find('Array')

            # Note to self: The mxGeometry might contain both polygon and line. This should be investigated. Which line is this?

            # Geometry contains coordinates for a line
            if source_point is not None and target_point is not None:
                x1, y1 = source_point.get('x'), source_point.get('y')
                x2, y2 = target_point.get('x'), target_point.get('y')
                multilinestring = f"({x1} {y1}, {x2} {y2})"
                multilinestrings.append(multilinestring)

            # Geometry contains coordinates for a polygon
            if points_array is not None:
                array_points = [(point.get('x'), point.get('y')) for point in points_array.findall('mxPoint')]
                polygon = ", ".join([f"{x} {y}" for x, y in array_points])
                polygons.append(f"(({polygon}))")

    combined_wkt = []
    if multilinestring:
        combined_wkt.append("MULTILINESTRING (" + ", ".join(multilinestrings) + ")")
    if polygons:
        combined_wkt.append("POLYGON " + ", ".join(polygons))

    return '\n'.join(combined_wkt)

if len(sys.argv) != 2 or sys.argv[1]=="-h":
    print("Usage: python3 xml_to_wkt_map.py <xml_file_path>")
    sys.exit(1)

xml_file_path = sys.argv[1]
wkt_output = parse_xml_to_wkt(xml_file_path)

# Map output file
output_file_path = 'map.wkt'
with open(output_file_path, 'w') as file:
    file.write(wkt_output)

print(f"WKT map data has been written to {output_file_path}")
