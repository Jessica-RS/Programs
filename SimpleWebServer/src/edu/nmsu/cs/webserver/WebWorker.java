package edu.nmsu.cs.webserver;

/**
 * Web worker: an object of this class executes in its own new thread to receive and respond to a
 * single HTTP request. After the constructor the object executes on its "run" method, and leaves
 * when it is done.
 *
 * One WebWorker object is only responsible for one client connection. This code uses Java threads
 * to parallelize the handling of clients: each WebWorker runs in its own thread. This means that
 * you can essentially just think about what is happening on one client at a time, ignoring the fact
 * that the entirety of the webserver execution might be handling other clients, too.
 *
 * This WebWorker class (i.e., an object of this class) is where all the client interaction is done.
 * The "run()" method is the beginning -- think of it as the "main()" for a client interaction. It
 * does three things in a row, invoking three methods in this class: it reads the incoming HTTP
 * request; it writes out an HTTP header to begin its response, and then it writes out some HTML
 * content for the response content. HTTP requests and responses are just lines of text (in a very
 * particular format).
 * 
 * @author Jon Cook, Ph.D.
 *
 **/

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WebWorker implements Runnable
{

	private Socket socket;
   private File file; 

	/**
	 * Constructor: must have a valid open socket
	 **/
	public WebWorker(Socket s)
	{
		socket = s;
	}

	/**
	 * Worker thread starting point. Each worker handles just one HTTP request and then returns, which
	 * destroys the thread. This method assumes that whoever created the worker created it with a
	 * valid open socket object.
	 **/
	public void run()
	{
   
		System.err.println("Handling connection...");
		try
		{
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			readHTTPRequest(is);
			writeHTTPHeader(os, "text/html");
			writeContent(os);
			os.flush();
			socket.close();
		}
		catch (Exception e)
		{
		e.printStackTrace();//	System.err.println("Output error: " + e);
		}
		System.err.println("Done handling connection.");
		return;
	}

	/**
	 * Read the HTTP request header.
	 **/
	private void readHTTPRequest(InputStream is)
	{
      
		String line;
		BufferedReader read = new BufferedReader(new InputStreamReader(is));
      
		while (true) //appears to be infinite loop
		{
			try
			{
				while (!read.ready())
					Thread.sleep(1);
				line = read.readLine();
            System.err.println("Request line: (" + line + ")");
				if (line.length() == 0)
					break;
            
            if(line.substring(0, 4).equals("GET ")) {
               String[] parts = line.split(" ");
               String path = "." + parts[1];//gets file name
               System.out.println(path);
               if (path.equals("./")) {
                  System.out.println("Success!");
                  path = "./text.html"; //set accepted file name
               }//end if
               
               file = new File(path);//set variable file to text.html
               
            }//end if
			}//end try
         
			catch (Exception e)
			{
				System.err.println("Request error: " + e);
				break;
            
			}//end catch
		}//end while
		return;
	}

	/**
	 * Write the HTTP header lines to the client network connection.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 * @param contentType
	 *          is the string MIME content type (e.g. "text/html")
	 **/
	private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
	{
      
		Date date = new Date();
		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(TimeZone.getTimeZone("GMT-7")); //set time zone to mountain time
      
      if(file.exists() && file.isFile()) {
         os.write("HTTP/1.1 200 OK\n".getBytes()); //if file is found throw 200 ok
      }//end if
      
      else {
         os.write("HTTP/1.1 404 Not Found\n".getBytes()); //if file is not found throw 404 error
      }//end else
      
		os.write("Date: ".getBytes());
		os.write((df.format(date)).getBytes());
		os.write("\n".getBytes());
		os.write("Server: Jess's awesome server\n".getBytes()); //custom server name
		os.write("Connection: close\n".getBytes());
		os.write("Content-Type: ".getBytes());
		os.write(contentType.getBytes());
		os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
		return;
	}

	/**
	 * Write the data content to the client network connection. This MUST be done after the HTTP
	 * header has been written out.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 **/
	private void writeContent(OutputStream os) throws Exception
	{
      //if file not found
      if(!file.exists() || !file.isFile()) { 
		   os.write("<html><head></head>".getBytes());
		   os.write("<body><h1>Error 404 Page Not Found</h1></html>\n".getBytes());//write 404 error on webpage
         return;
      }//end if
      
      //if file is found successfully
      else{
         BufferedReader buff = new BufferedReader(new FileReader(file));
         String sub;
         Date date = new Date();
         DateFormat df = DateFormat.getDateTimeInstance();
         df.setTimeZone(TimeZone.getTimeZone("GMT-7"));
         while ((sub = buff.readLine()) != null) {
            sub = sub.replaceAll("<cs371date>", df.format(date)); //replace tags with date
            sub = sub.replaceAll("<cs371server>", "Jess's Awesome Server");//replace tags with server name
            os.write(sub.getBytes());//write date and server name
         }//end while
         
         buff.close();
         
      }//end else
	}//end writeContent
} // end class
