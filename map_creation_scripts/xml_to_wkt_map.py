'''
This script will turn an xml file exported from draw.io containing lines and polygons
into a representation in the "Well-known text representation of geometry" (WKT) format.
It will read the file path to the xml file as it's only argument and output the file map.wkt.
The script will simply overwrite map.wkt, should it already exist.
'''

import xml.etree.ElementTree as ET
import sys
import os


def parse_xml_to_wkt(xml_file_path):
    global linestrings
    # linestrings that are not for moving, just for connecting other lines
    global connecting_linestrings
    global polygons

    # As the map contains multiple seperate lines, these lines will be save in a multilinestring
    linestrings = []
    polygons = []
    connecting_linestrings = []

    tree = ET.parse(xml_file_path)
    root = tree.getroot()

    for mxCell in root.iter('mxCell'):
        # The map contains lines that are just there to connect the different lines. We want to ignore these. Lines with
        # "dashPattern=1" in the style field will be lines that nodes can move on
        cell_style_attribute = mxCell.get('style')

        geometry = mxCell.find('mxGeometry')
        if geometry is not None:
            # Geometry might contain two points that make up a line and additionally an array of points that make up a polygon
            source_point = geometry.find('mxPoint[@as="sourcePoint"]')
            target_point = geometry.find('mxPoint[@as="targetPoint"]')
            points_array = geometry.find('Array')

            if source_point is not None and target_point is not None:
                x1, y1 = source_point.get('x'), (1000 - int(source_point.get('y')))
                x2, y2 = target_point.get('x'), (1000 - int(target_point.get('y')))

                linestring = f"LINESTRING ({x1} {y1}, {x2} {y2})"
                # The polygon is usually defined together with a line that is just made up of two identical points.
                # We want to ignore that line so that it does not have to be connected to the other lines
                if x1 != x2 or y1 != y2:
                    if cell_style_attribute is not None and "dashPattern" in cell_style_attribute:
                        linestrings.append(linestring)
                    else:
                        connecting_linestrings.append(linestring)

            # Geometry contains coordinates for a polygon
            if points_array is not None:
                array_points = [(point.get('x'), point.get('y')) for point in points_array.findall('mxPoint')]
                polygon = ", ".join([f"{x} {y}" for x, y in array_points])
                polygons.append(f"POLYGON(({polygon}))")

if len(sys.argv) != 2 or sys.argv[1]=="-h":
    print("Usage: python3 xml_to_wkt_map.py <xml_file_path>")
    sys.exit(1)

# File path is given as second argument by user
xml_file_path = sys.argv[1]

parse_xml_to_wkt(xml_file_path)

# Extract filename from filepath given by user
filename_without_extension, _ = os.path.splitext(os.path.basename(xml_file_path))

# Counter for counting the number of map files used
map_file_counter = 1

number_of_linestrings = 0
number_of_linestrings = 0
number_of_polygons = 0

# Write one multilinestring to one file
if linestrings:
    number_of_linestrings = len(linestrings)
    for i in range(number_of_linestrings-1):
        # Output linestring file
        with open(filename_without_extension + f"_line{i}.wkt", 'w') as file:
            file.write(linestrings[i])
        # Print text to copy directly into config file
        print(f"MapBasedMovement.mapFile{map_file_counter} = data/cluster/"
              + filename_without_extension + f"_line{i}.wkt")
        map_file_counter += 1

# Write one multilinestring to one file
if connecting_linestrings:
    number_of_linestrings = len(connecting_linestrings)
    for i in range(number_of_linestrings-1):
        # Output linestring file
        with open(filename_without_extension + f"_connecting_line{i}.wkt", 'w') as file:
            file.write(connecting_linestrings[i])
        print(f"MapBasedMovement.mapFile{map_file_counter} = data/cluster/"
              + filename_without_extension + f"_connecting_line{i}.wkt")
        map_file_counter += 1

# Write one polygon to one file
if polygons:
    number_of_polygons = len(polygons)
    for i in range(number_of_polygons-1):
        # Output linestring file
        with open(filename_without_extension + f"_polygon{i}.wkt", 'w') as file:
            file.write(polygons[i])
        print(f"MapBasedMovement.mapFile{map_file_counter} = data/cluster/"
              + filename_without_extension + f"_polygon{i}.wkt")
        map_file_counter += 1
print(f"MapBasedMovement.nrofMapFiles = {map_file_counter - 1}")
print(f"WKT map data for {number_of_linestrings} lines has been written.")
print(f"WKT map data for {number_of_linestrings} connecting lines has been written.")
print(f"WKT map data for {number_of_polygons} polygons has been written.")