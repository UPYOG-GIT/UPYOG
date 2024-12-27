import React, { useState } from "react";
import ReactMapGL, { NavigationControl, Source, Layer, Marker } from "react-map-gl";
import proj4 from "proj4";
import { processDXFFile } from "./dxfProcessor"; // Assuming you have a dxfProcessor.js file
import axios from "axios";

const MapBoxGIS = () => {
  const [viewport, setViewport] = useState({
    latitude: 21.221276,
    longitude: 81.651317,
    zoom: 18,
    width: "100vw",
    height: "100vh",
  });
  const [geojsonData, setGeojsonData] = useState(null);
  const [permanentFeatures, setPermanentFeatures] = useState([]);
  const [coordinates, setCoordinates] = useState([]); // To store all coordinates
  const [searchQuery, setSearchQuery] = useState("");
  const [mapboxAccessToken] = useState(
    "pk.eyJ1IjoiYmh1cGVzaGVudGl0IiwiYSI6ImNtM3IxeGY0djAxNDkybHI3NGlyOHZka3MifQ.DJ44H3NxwKpu6cy5UZ06AA"
  );

  const sourceCRS = "+proj=utm +zone=33 +datum=WGS84";
  const targetCRS = "EPSG:4326";

  const handleSearchChange = (event) => {
    setSearchQuery(event.target.value);
  };

  const handleSearchSubmit = async (event) => {
    event.preventDefault();
    if (!searchQuery) {
      alert("Please enter a search query.");
      return;
    }

    const latLonRegex = /^-?\d+(\.\d+)?\s*,\s*-?\d+(\.\d+)?$/;
    if (latLonRegex.test(searchQuery)) {
      const [lat, lon] = searchQuery.split(",").map(Number);
      if (!isNaN(lat) && !isNaN(lon)) {
        setViewport({
          ...viewport,
          latitude: lat,
          longitude: lon,
          zoom: 18,
        });
      } else {
        alert("Invalid latitude and longitude format.");
      }
    } else {
      try {
        const response = await axios.get(
          `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(searchQuery)}.json`,
          {
            params: {
              access_token: "pk.eyJ1IjoiYmh1cGVzaGVudGl0IiwiYSI6ImNtM3IxeGY0djAxNDkybHI3NGlyOHZka3MifQ.DJ44H3NxwKpu6cy5UZ06AA",
              limit: 1,
            },
          }
        );

        if (response.data && response.data.features.length > 0) {
          const [lon, lat] = response.data.features[0].center;
          setViewport({
            ...viewport,
            latitude: lat,
            longitude: lon,
            zoom: 16,
          });
        } else {
          alert("No results found for the place entered.");
        }
      } catch (error) {
        alert("Error fetching place data.");
      }
    }
  };

  const handleFileUpload = (event) => {
    const file = event.target.files[0];
    if (!file) {
      alert("No file selected.");
      return;
    }

    const userCoordinate = window.prompt(
      "Enter target coordinate (latitude, longitude) in the format 'lat,lon':"
    );
    if (!userCoordinate) {
      alert("No coordinate entered.");
      return;
    }

    const [targetLat, targetLon] = userCoordinate.split(",").map(Number);
    if (isNaN(targetLat) || isNaN(targetLon)) {
      alert("Invalid coordinate format.");
      return;
    }

    setGeojsonData(null);
    setPermanentFeatures([]);
    setCoordinates([]); // Reset all coordinates

    const reader = new FileReader();
    reader.onload = async (e) => {
      try {
        const content = e.target.result;
        const geojson = await processDXFFile(content);
        if (geojson) {
          const allCoordinates = []; // Collect all coordinates here
          const firstCoord = geojson.features[0]?.geometry.coordinates[0];
          if (firstCoord) {
            const [firstX, firstY] = firstCoord;
            const [firstLon, firstLat] = proj4(sourceCRS, targetCRS, [firstX, firstY]);

            const latOffset = targetLat - firstLat;
            const lonOffset = targetLon - firstLon;

            geojson.features.forEach((feature) => {
              feature.geometry.coordinates = feature.geometry.coordinates.map(([x, y]) => {
                const [lon, lat] = proj4(sourceCRS, targetCRS, [x, y]);
                const adjustedCoord = [lon + lonOffset, lat + latOffset];
                allCoordinates.push(adjustedCoord); // Add each coordinate to the collection
                return adjustedCoord;
              });
            });
          }

          setGeojsonData(geojson);
          setPermanentFeatures(geojson.features);
          setCoordinates(allCoordinates); // Store all coordinates in state

          console.log("All Coordinates:", allCoordinates); // Log all coordinates
        } else {
          alert("Failed to process DXF file.");
        }
      } catch (err) {
        alert("Error processing DXF file.");
      }
    };
    reader.readAsText(file);
  };

  return (
    <div style={{ position: "relative", width: "100vw", height: "100vh" }}>
      <div style={{ position: "absolute", top: 10, left: 10, zIndex: 1 }}>
        <form onSubmit={handleSearchSubmit}>
          <input
            type="text"
            value={searchQuery}
            onChange={handleSearchChange}
            placeholder="Enter place name or Latitude, Longitude"
          />
          <button type="submit">Search</button>
        </form>
      </div>

      <div style={{ position: "absolute", top: 50, left: 10, zIndex: 1 }}>
        <input type="file" accept=".dxf" onChange={handleFileUpload} />
      </div>

      <ReactMapGL
        {...viewport}
        mapStyle="mapbox://styles/mapbox/satellite-v9"
        mapboxAccessToken={"pk.eyJ1IjoiYmh1cGVzaGVudGl0IiwiYSI6ImNtM3IxeGY0djAxNDkybHI3NGlyOHZka3MifQ.DJ44H3NxwKpu6cy5UZ06AA"}
        onMove={(evt) => setViewport(evt.viewState)}
      >
        <div style={{ position: "absolute", top: 50, left: 10 }}>
          <NavigationControl />
        </div>

        {geojsonData && (
          <Source id="dxf-source" type="geojson" data={geojsonData}>
            <Layer
              id="dxf-layer"
              type="line"
              paint={{
                "line-color": "#ff0000",
                "line-width": 2,
              }}
            />
          </Source>
        )}
      </ReactMapGL>
    </div>
  );
};

export default MapBoxGIS;
