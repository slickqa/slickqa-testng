package com.slickqa.testng;

import com.slickqa.client.errors.SlickError;
import com.slickqa.client.model.Result;
import com.slickqa.client.model.StoredFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by slambson on 7/13/17.
 */
public class SlickFileAttacher {

    protected SlickResult slick;

    public SlickFileAttacher(SlickResult slick) {
        this.slick = slick;
    }

    private void addFileToResult(String resultId, StoredFile file) {
        try {
            Result current = slick.getSlickClient().result(resultId).get();
            List<StoredFile> files = current.getFiles();
            if(files == null) {
                files = new ArrayList<>(1);
            }
            files.add(file);
            Result update = new Result();
            update.setFiles(files);
            slick.getSlickClient().result(current.getId()).update(update);
        } catch (SlickError e) {
            e.printStackTrace();
            System.err.println("!! ERROR: adding file to result " + resultId + " !!");
        }

    }

    public void addFile(Path localPath) {
        if(slick.isUsingSlick()) {
            Result current = slick.getCurrentResult();
            if(current != null) {
                StoredFile file = null;
                try {
                    file = slick.getSlickClient().files().createAndUpload(localPath);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!! ERROR: unable to upload file " + localPath.toString() + " !!");
                }
                if (file != null) {
                    addFileToResult(current.getId(), file);
                }
            } else {
                System.err.println("!! WARNING: no current result when trying to add " + localPath.toString() + " !!");
            }
        }
    }

    public void addFile(String filename, String mimetype, InputStream inputStream) {
        if(slick.isUsingSlick()) {
            Result current = slick.getCurrentResult();
            if(current != null) {
                StoredFile file = null;
                try {
                    file = slick.getSlickClient().files().createAndUpload(filename, mimetype, inputStream);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!! ERROR: unable to upload file " + filename + " !!");
                }
                if (file != null) {
                    addFileToResult(current.getId(), file);
                }
            } else {
                System.err.println("!! WARNING: no current result when trying to add " + filename + " !!");
            }
        }
    }
}
