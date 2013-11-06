package edu.arizona.sirls.etc.site.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.arizona.sirls.etc.site.server.rpc.AuthenticationService;
import edu.arizona.sirls.etc.site.shared.rpc.AuthenticationResult;
import edu.arizona.sirls.etc.site.shared.rpc.AuthenticationToken;
import edu.arizona.sirls.etc.site.shared.rpc.IAuthenticationService;
import edu.arizona.sirls.etc.site.shared.rpc.RPCResult;

public class DownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 6688917446498637833L;
	private IAuthenticationService authenticationService = new AuthenticationService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String sessionID = request.getParameter("sessionID");
		String target = request.getParameter("target");
		String directory = request.getParameter("directory");
		
		RPCResult<AuthenticationResult> authenticationResult = 
				authenticationService.isValidSession(new AuthenticationToken(username, sessionID));
		if(authenticationResult.isSucceeded() && authenticationResult.getData().getResult()) { 	
			int BUFFER = 1024 * 100;
			response.setContentType("application/octet-stream");
			if(directory.equals("yes")) {
				target = target + ".tar.gz";
				response.setHeader("Content-Disposition:", "attachment;filename=" + "\"" + 
						target.substring(target.lastIndexOf(File.separator) + 2, target.length()) + "\"");	
			} else {
				response.setHeader("Content-Disposition:", "attachment;filename=" + "\"" + 
						target.substring(target.lastIndexOf(File.separator) + 2, target.length()) + "\"");	
			}
			
			ServletOutputStream outputStream = response.getOutputStream();
			
			byte[] fileBytes;
			if(directory.equals("yes")) {
				fileBytes = getZipFile(username, target);
			} else {
				fileBytes = getFile(username, target);
			}
			
			response.setContentLength(Long.valueOf(fileBytes.length).intValue());
			response.setBufferSize(BUFFER);
			outputStream.write(fileBytes);
			outputStream.flush();
			//outputStream.close();
		}
	}

	private byte[] getZipFile(String username, String target) throws IOException {
		Path path = Paths.get(Configuration.zipFileBase + File.separator + username + File.separator + target);
		byte[] data = Files.readAllBytes(path);
		return data;
	}

	private byte[] getFile(String username, String target) throws IOException {		
		//Path path = Paths.get(Configuration.fileBase + File.separator + username + File.separator + target);
		Path path = Paths.get(target);
		byte[] data = Files.readAllBytes(path);
		return data;
	}
}