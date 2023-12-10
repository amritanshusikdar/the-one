'''
This script will turn an xml file exported from draw.io containing lines and polygons
into a representation in the "Well-known text representation of geometry" (WKT) format.
It will read the file path to the xml file as it's only argument and output the file map.wkt.
The script will simply overwrite map.wkt, should it already exist.
'''

import xml.etree.ElementTree as ET
import sys
import os

combined_wkt_multilinestring = ""
combined_wkt_polygon = ""

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
                multilinestring = f"({x1} -{y1}, {x2} -{y2})"
                multilinestrings.append(multilinestring)

            # Geometry contains coordinates for a polygon
            if points_array is not None:
                array_points = [(point.get('x'), point.get('y')) for point in points_array.findall('mxPoint')]
                polygon = ", ".join([f"{x} -{y}" for x, y in array_points])
                polygons.append(f"(({polygon}))")

    if multilinestring:
        combined_wkt_multilinestring = "MULTILINESTRING (" + ", ".join(multilinestrings) + ")"
    if polygons:
        combined_wkt_polygon = "POLYGON " + ", ".join(polygons)

if len(sys.argv) != 2 or sys.argv[1]=="-h":
    print("Usage: python3 xml_to_wkt_map.py <xml_file_path>")
    sys.exit(1)

# File path is given as second argument by user
xml_file_path = sys.argv[1]

parse_xml_to_wkt(xml_file_path)

# Extract filename from filepath given by user
filename_without_extension, _ = os.path.splitext(os.path.basename(xml_file_path))
# Output´files are different for MULTILINESTRINGS and POLYGONS
output_file_path_multilinestring = filename_without_extension + "_multilinestring.wkt"
output_file_path_polygon = filename_without_extension + "_polygon.wkt"

# Output MULTILINESTRING file
with open(output_file_path_multilinestring, 'w') as file:
    file.write(combined_wkt_multilinestring)

# Output POLYGON file
with open(output_file_path_polygon, 'w') as file:
    file.write(combined_wkt_polygon)

print(f"WKT map data has been written to {output_file_path_multilinestring} and {output_file_path_polygon}.")
