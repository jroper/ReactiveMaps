define ["jquery"], () ->
  {
    userDistance: (email) ->
      $.getJSON("/user/" + email + "/distance").then (data) ->
        data.distance
  }