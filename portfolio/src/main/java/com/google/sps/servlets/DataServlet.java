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
    if (userService.isUserLoggedIn()) {
      Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

      String strNumComments = request.getParameter("numComments");
      int numComments;
      try{ 
        numComments = Integer.parseInt(strNumComments);
        if(numComments <= 0) {
          throw new Exception("Invalid number of comments.");
        }
      } catch (Exception e) { 
        JSONObject errMessage = new JSONObject();
        errMessage.put("message", "Invalid number of comments");
        JSONObject err = new JSONObject();
        err.put("error", errMessage);
        response.getWriter().println(errMessage);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(numComments));

      ArrayList<Comment> comments = new ArrayList<>();
      for (Entity entity : results) {
        long id = entity.getKey().getId();
        String text = (String) entity.getProperty("comment");
        String user = (String) entity.getProperty("user");
        comments.add(new Comment(id, text, user));
      }
      String logoutUrl = userService.createLogoutURL("/#comments");
	  JSONObject commentsJson = new JSONObject();
      commentsJson.put("comments", convertToJson(comments));
      commentsJson.put("loggedin", true);
      commentsJson.put("user", userService.getCurrentUser().getEmail());
      commentsJson.put("url", logoutUrl);
      response.setContentType("text/html;");
      response.getWriter().println(commentsJson);
    } else {
      JSONObject commentsJson = new JSONObject();
      commentsJson.put("loggedin", false);
      String loginUrl = userService.createLoginURL("/#comments");
      commentsJson.put("url", loginUrl);
      response.setContentType("text/html;");
      response.getWriter().println(commentsJson);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      // Get the input from the form.
      String comment = request.getParameter("text-input");
      long timestamp = System.currentTimeMillis();

      if (comment != null && !comment.isEmpty()){ 
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
