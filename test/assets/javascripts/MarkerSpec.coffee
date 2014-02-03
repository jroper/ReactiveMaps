requirejs = require "requirejs"
requirejs.config {
  nodeRequire: require
  baseUrl: __dirname
}

Squire = requirejs "Squire"
assert = requirejs "assert"

# Mocks
class MockPromise
  constructor: (value) ->
    @value = value
  done: (callback) ->
    callback(@value)

class LatLng
  constructor: (lat, lng) ->
    @lat = lat
    @lng = lng

class MockPopup
  constructor: (content) ->
    @content = content
  setContent: (content) ->
    @content = content
    @
  update: ->

class MockMarker
  constructor: (latLng, options) ->
    @latLng = latLng
    @options = options
  bindPopup: (content) ->
    @popup = new MockPopup(content)
  on: (type, callback) ->
    @onClick = callback
  getPopup: ->
    @popup
  setLatLng: (latLng) ->
    @latLng = latLng
  setIcon: (icon) ->
    @options.icon = icon
  addTo: (map) ->
    @addedTo = map

class MockMarkerRenderer
  renderPopup: (userId, distance) ->
    userId + ":" + distance
  createClusterMarkerIcon: (count) ->
    "cluster of " + count
  resetTranstion: ->
  transition: ->

class MockUserInfo
  users: {}
  userDistance: (userId) ->
    new MockPromise(@users[userId])

class MockLeaflet
  Marker: MockMarker
  LatLng: LatLng

class MockMap

testMarker = (test) ->
  (done) ->

    # Create mocks
    leaflet = new MockLeaflet()
    renderer = new MockMarkerRenderer()
    userInfo = new MockUserInfo()

    # Mockout require js environment
    new Squire()
    .mock("markerRenderer", renderer)
    .mock("userInfo", userInfo)
    .mock("leaflet", leaflet)
    .require ["./models/marker"], (Marker) ->
        test({
          leaflet: leaflet,
          renderer: renderer,
          userInfo: userInfo
        }, Marker, done)

cluster = {
  properties: {
    count: 10
    timestamp: 0
  }
  id: "somecluster"
}

single = {
  properties: {
    timestamp: 0
  }
  id: "userid"
}

describe "Marker", ->
  it "should create a single marker", testMarker (deps, Marker, done) ->
    marker = new Marker(new MockMap(), single, new LatLng(10, 20))
    assert.equal(single, marker.feature)
    assert.deepEqual(new LatLng(10, 20), marker.marker.latLng)
    assert.equal("userid:undefined", marker.marker.popup.content)
    done()

  it "should create a cluster marker", testMarker (deps, Marker, done) ->
    marker = new Marker(new MockMap(), cluster, new LatLng(10, 20))
    assert.equal(cluster, marker.feature)
    assert.deepEqual(new LatLng(10, 20), marker.marker.latLng)
    assert.equal("cluster of 10", marker.marker.options.icon)
    done()

  it "should add it to the map", testMarker (deps, Marker, done) ->
    map = new MockMap()
    marker = new Marker(map, single, new LatLng(10, 20))
    assert.equal(map, marker.marker.addedTo)
    done()

  it "should update the position", testMarker (deps, Marker, done) ->
    marker = new Marker(new MockMap(), single, new LatLng(10, 20))
    marker.update({
      properties: {
        timestamp: 0
      }
      id: "userid"
    }, new LatLng(20, 30))
    assert.deepEqual(new LatLng(20, 30), marker.marker.latLng)
    done()

  it "should update the cluster count", testMarker (deps, Marker, done) ->
    marker = new Marker(new MockMap(), cluster, new LatLng(10, 20))
    marker.update({
      properties: {
        timestamp: 0
        count: 20
      }
      id: "somecluster"
    }, new LatLng(20, 30))
    assert.equal("cluster of 20", marker.marker.options.icon)
    done()

  it "should update the popup with the current distance when clicked", testMarker (deps, Marker, done) ->
    marker = new Marker(new MockMap(), single, new LatLng(10, 20))
    deps.userInfo.users["userid"] = 50
    marker.marker.onClick()
    assert.equal("userid:50", marker.marker.popup.content)
    done()
