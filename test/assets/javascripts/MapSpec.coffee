requirejs = require "requirejs"
requirejs.config {
    nodeRequire: require
    baseUrl: __dirname
}

Squire = requirejs "Squire"
assert = requirejs "assert"

# Mocks
class MockLeaflet
  constructor: () ->
    self = @
    @TileLayer = class
      addTo: (map) ->
        self._addedTo = map

  _map: {
    foo: "bar"
    setView: (center, zoom) ->
      @_center = center
      @_zoom = zoom
    on: (event, fn) ->
    remove: () ->
  }
  map: ->
    @_map

class MockStorage
  area: null;
  lastArea: ->
    @area
  setLastArea: (area) ->
    @area = area

class MockMarker

# Tests
testMap = (test) ->
  (done) ->

    # Create mocks
    leaflet = new MockLeaflet()
    storage = new MockStorage()

    # Mockout require js environment
    new Squire()
      .mock("marker", MockMarker)
      .mock("storage", storage)
      .mock("leaflet", leaflet)
      .require ["./models/map"], (Map) ->
        test(leaflet, storage, Map, done)

describe "Map", ->

  it "should create a tile layer", testMap (leaflet, storage, Map, done) ->
    new Map().destroy()
    assert.equal leaflet._map, leaflet._addedTo
    done()

  it "should initialise the map view", testMap (leaflet, storage, Map, done) ->
    new Map().destroy()
    assert.equal 0, leaflet._map._center[0]
    assert.equal 0, leaflet._map._center[1]
    assert.equal 2, leaflet._map._zoom
    done()

  it "should initialise the map view to the last stored area", testMap (leaflet, storage, Map, done) ->
    storage.setLastArea({center: [1, 2], zoom: 3})
    new Map().destroy()
    assert.equal 1, leaflet._map._center[0]
    assert.equal 2, leaflet._map._center[1]
    assert.equal 3, leaflet._map._zoom
    done()

