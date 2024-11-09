package dev.resteasy.grpc.lists.sets;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

public class DD5 {

   @Path("d1")
   @POST
   public D1 d1(D1 d1) {
      return d1;
   }
   
   @Path("d2")
   @POST
   public D2 d2(D2 d2) {
      return d2;
   }
   
   @Path("d3")
   @POST
   public D3 d3(D3 d3) {
      return d3;
   }
   
   @Path("d4")
   @POST
   public D4 d4(D4 d4) {
      return d4;
   }
   
   @Path("d5")
   @POST
   public D5 d5(D5 d5) {
      return d5;
   }
}
