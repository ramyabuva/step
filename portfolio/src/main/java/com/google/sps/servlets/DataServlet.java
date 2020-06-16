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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
import org.json.simple.JSONObject;


/** Servlet that returns some example content.*/
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    JSONObject commentsJson = new JSONObject();
    if (userService.isUserLoggedIn()) {
      commentsJson = getLoggedInComments(request.getParameter("numComments"), userService);
    } else {
      commentsJson = getLoggedOutComments(userService);
    }
    if (commentsJson.get("error") != null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    response.setContentType("text/html;");
    response.getWriter().println(commentsJson);
  }

  public JSONObject getLoggedInComments(String strNumComments, UserService userService) {
    int numComments;
    try { 
      numComments = Integer.parseInt(strNumComments);
      if (numComments <= 0) {
        throw new Exception("Invalid number of comments.");
      }
    } catch (Exception e) { 
      JSONObject errMessage = new JSONObject();
      errMessage.put("message", "Invalid number of comments");
      JSONObject err = new JSONObject();
      err.put("error", errMessage);
      return err;
    }

    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(numComments));

    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity entity : results) {
      long id = entity.getKey().getId();
      String text = (String) entity.getProperty("comment");
      String user = (String) entity.getProperty("user");
      comments.add(new Comment(id, text, user));
    }
    JSONObject commentsJson = new JSONObject();
    commentsJson.put("comments", convertToJson(comments));
    commentsJson.put("logged_in", true);
    commentsJson.put("user", userService.getCurrentUser().getEmail());
    commentsJson.put("url", userService.createLogoutURL("/#comments"));
    return commentsJson;
  }

  public JSONObject getLoggedOutComments(UserService userService) { 
    JSONObject commentsJson = new JSONObject();
    JSONObject errMessage = new JSONObject();
    errMessage.put("message", "User is not logged in");
    commentsJson.put("error", errMessage);
    commentsJson.put("logged_in", false);
    commentsJson.put("url", userService.createLoginURL("/#comments"));
    return commentsJson;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      // Get the input from the form.
      String comment = request.getParameter("text-input");
      long timestamp = System.currentTimeMillis();

      if (comment != null && !comment.isEmpty()) { 
        Entity commentsEntity = new Entity("Comment");
        commentsEntity.setProperty("comment", comment);
        commentsEntity.setProperty("timestamp", timestamp);
        commentsEntity.setProperty("user", userService.getCurrentUser().getEmail());  

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentsEntity);
      }
    }
    response.sendRedirect("/#comments");
  }

  /**
   * Converts a ServerStats instance into a JSON string using the Gson library. Note: We first added
   * the Gson library dependency to pom.xml.
   */
  private String convertToJson(ArrayList<Comment> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }
}
