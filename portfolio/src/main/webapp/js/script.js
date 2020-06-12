// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
var mode = false;

/**
 * Sets Light mode of page based on cookies
 */
function setMode() { 
  const value = getCookie('mode');
  if (value == 'dark') {
    switchSheet('css/nightmode.css');
    switchIcon('fas fa-sun');
    mode = true;
  } else {
    switchSheet('');
    switchIcon('fas fa-moon');
    mode = false;
  }
}

/**
 * @param {string} The string whose value you want to get from the cookie. 
 */
function getCookie(name) { 
  const re = new RegExp(name + '=([^;]+)');
  const value = re.exec(document.cookie);
  return (value != null) ? unescape(value[1]) : null;
}

/**
 * Switches Light Mode of Page
 */
function switchMode() {
  if (mode) { 
    document.cookie = "mode=light";
  } else { 
    document.cookie = "mode=dark";
  }
  setMode();
  createMap();
}

/**
 * Switches Stylesheet of the Page
 * @param {string} mode The path to the desired mode stylesheet.
 */
function switchSheet(mode) { 
  document.getElementById('pagestyle').setAttribute('href', mode);  
}

/**
 * Changes icon for Light Mode button
 * @param {string} icon The string code for the fabulous fonts icon to display.
 */
function switchIcon(icon) { 
  document.getElementById('modeicon').setAttribute('class', icon);  
}

/**
 * Get number of comments from data servlet
 * @param {string} numComments A string equivalent of the number of comments in datastore.
 */
async function getNumComments(numComments) {
  const response = await fetch(`/data?numComments=${numComments}`);
  const messages = await response.json();
  const messageContainer = document.getElementById('message-container');
  messageContainer.innerHTML = '';
  for (const message of messages) { 
    messageContainer.appendChild(createListElement(message));
  }
}

/**
 * Display text from data Servlet
 */
async function getText() {
  setMode();
  const numComments = document.getElementById('number-comments');
  getNumComments(numComments.options[numComments.selectedIndex].value);
  createMap();
}

/** Creates an <li> element containing text.
 * @param {Object} message A JSON object representing a single comment.
 */
function createListElement(message) {
  const liElement = document.createElement('li');
  liElement.className = 'message';
  liElement.innerText = message.text;
  liElement.setAttribute('class', 'list-group-item');

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerHTML = '<i class="fas fa-trash-alt" id="modeicon"></i>';
  deleteButtonElement.setAttribute('class', 'social-icon float-right');
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(message);

    // Remove the comment from the DOM.
    liElement.remove();
  });

  liElement.appendChild(deleteButtonElement);
  return liElement;
}

/** 
 * Tells the server to delete the comment. 
 * @param {Object} message A JSON object representing a single comment.
*/
function deleteComment(message) {
  const params = new URLSearchParams();
  params.append('id', message.id);
  fetch('/delete-comment', {method: 'POST', body: params});
  getText();
}


/** Creates a map and adds it to the page. */
function createMap() {
  const nightmode = [
    {elementType: 'geometry', stylers: [{color: '#242f3e'}]},
    {elementType: 'labels.text.stroke', stylers: [{color: '#242f3e'}]},
    {elementType: 'labels.text.fill', stylers: [{color: '#746855'}]},
    {
      featureType: 'administrative.locality',
      elementType: 'labels.text.fill',
      stylers: [{color: '#d59563'}]
    },
    {
      featureType: 'poi',
      elementType: 'labels.text.fill',
      stylers: [{color: '#d59563'}]
    },
    {
      featureType: 'poi.park',
      elementType: 'geometry',
      stylers: [{color: '#263c3f'}]
    },
    {
      featureType: 'poi.park',
      elementType: 'labels.text.fill',
      stylers: [{color: '#6b9a76'}]
    },
    {
      featureType: 'road',
      elementType: 'geometry',
      stylers: [{color: '#38414e'}]
    },
    {
      featureType: 'road',
      elementType: 'geometry.stroke',
      stylers: [{color: '#212a37'}]
    },
    {
      featureType: 'road',
      elementType: 'labels.text.fill',
      stylers: [{color: '#9ca5b3'}]
    },
    {
      featureType: 'road.highway',
      elementType: 'geometry',
      stylers: [{color: '#746855'}]
    },
    {
      featureType: 'road.highway',
      elementType: 'geometry.stroke',
      stylers: [{color: '#1f2835'}]
    },
    {
      featureType: 'road.highway',
      elementType: 'labels.text.fill',
      stylers: [{color: '#f3d19c'}]
    },
    {
      featureType: 'transit',
      elementType: 'geometry',
      stylers: [{color: '#2f3948'}]
    },
    {
      featureType: 'transit.station',
      elementType: 'labels.text.fill',
      stylers: [{color: '#d59563'}]
    },
    {
      featureType: 'water',
      elementType: 'geometry',
      stylers: [{color: '#17263c'}]
    },
    {
      featureType: 'water',
      elementType: 'labels.text.fill',
      stylers: [{color: '#515c6d'}]
    },
    {
      featureType: 'water',
      elementType: 'labels.text.stroke',
      stylers: [{color: '#17263c'}]
    }
  ]

  // set style of map according to mode
  let mapUniversity;
  let mapHighSchool;
  if (mode) { 
    mapUniversity = new google.maps.Map(
        document.getElementById('map-uva'),
        {center: {lat: 38.035546, lng: -78.503425}, zoom: 16, styles: nightmode});
    mapHighSchool = new google.maps.Map(
      document.getElementById('map-rr'),
      {center: {lat: 38.977849, lng: -77.499964}, zoom: 16, styles: nightmode});
  } else {
    mapUniversity = new google.maps.Map(
        document.getElementById('map-uva'),
        {center: {lat: 38.035546, lng: -78.503425}, zoom: 16});
    mapHighSchool = new google.maps.Map(
      document.getElementById('map-rr'),
      {center: {lat: 38.977849, lng: -77.499964}, zoom: 16});
  }

  let markerUniversity = new google.maps.Marker({
    position: {lat: 38.035546, lng: -78.503425},
    map: mapUniversity,
    title: 'University of Virginia'
  });
  const universityInfoWindow =
      new google.maps.InfoWindow({content: 'This is the Rotunda, the primary symbol of the university'});
  markerUniversity.addListener('click', function() {
    mapUniversity.setZoom(20);
    mapUniversity.setCenter(markerUniversity.getPosition());
    universityInfoWindow.open(mapUniversity, markerUniversity);
  });

  let markerHighSchool = new google.maps.Marker({
    position: {lat: 38.977849, lng: -77.499964},
    map: mapHighSchool,
    title: 'Rock Ridge High School'
  });
  markerHighSchool.addListener('click', function() {
    mapHighSchool.setZoom(20);
    mapHighSchool.setCenter(markerHighSchool.getPosition());
  });
}
