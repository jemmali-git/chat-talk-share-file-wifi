/*          Copyright © 2015-2016 Stanislav Petriakov
// Distributed under the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE_1_0.txt or copy at
//          http://www.boost.org/LICENSE_1_0.txt)
*/
package com.jemali.nadhem.chat_and_share;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FileChooser extends DialogFragment implements DialogInterface.OnClickListener {
    public static final int PERMISSION_REQUEST = 573;

    private FileChooserListener mSFCListener;
    private String mRootPath, mCurrentPath;
    private ArrayList<String> mDirs;
    private ArrayAdapter<String> mAdapter;
    private ListView mListViewDirs;
    private boolean mShowHidden = true;

    public interface FileChooserListener {
        void onFileChosen(File file);
        void onDirectoryChosen(File directory);
        void onCancel();
    }

    /**
     * Check is read permission granted
     */
    public static boolean isPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check is permission request result is OK
     */
    public static boolean isGrantResultOk(int[] grantResults) {
        return grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request read permission (need since API 23)
     *
     * @param activity
     * Root activity
     */
    public void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE))
                Toast.makeText(activity, "Please grant read storage permission", Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mCurrentPath = mRootPath = mRootPath == null ? Environment.getExternalStorageDirectory().getAbsolutePath() : mRootPath;

        try {
            mRootPath = new File(mRootPath).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mDirs = getFilesInDirectory(mRootPath);
        mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, mDirs);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setSingleChoiceItems(mAdapter, -1, this).setPositiveButton(android.R.string.ok, this).
                setNegativeButton(android.R.string.cancel, this).setTitle(getDirectoryName());

        AlertDialog alert = builder.create();
        mListViewDirs = alert.getListView();

        return alert;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        File current = new File(mCurrentPath);

        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                if (current.isDirectory())
                    mSFCListener.onDirectoryChosen(current);

                if (current.isFile())
                    mSFCListener.onFileChosen(current);
                break;
            case Dialog.BUTTON_NEGATIVE:
                mSFCListener.onCancel();
                break;
            default:
                selectFile(which);
                break;
        }
    }

    /**
     * Select new file or directory
     */
    private void selectFile(int which) {
        File current = new File(mCurrentPath);

        try {
            //noinspection ResultOfMethodCallIgnored
            current.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (current.isFile())
            mCurrentPath = mCurrentPath.substring(0, mCurrentPath.lastIndexOf("/"));

        String selected = mDirs.get(which).replace("/", "");

        if (selected.equals("..")) {
            mCurrentPath = mCurrentPath.substring(0, mCurrentPath.lastIndexOf("/"));
            refreshListView();
        } else {
            mCurrentPath += "/" + selected;
            current = new File(mCurrentPath);

            if (!current.isFile())
                refreshListView();
        }

       /* if (!current.isFile())
        {
            String root="<font color=\"red\">"+getDirectoryName()+"</font> ";
           // name.setText(Html.fromHtml(format_string));
            getDialog().setTitle(Html.fromHtml(root));///////////////here
        }*/
        getDialog().setTitle(getDirectoryName());
    }

    /**
     * Update ListView's items
     */
    private void refreshListView() {
        mDirs.clear();
        mDirs.addAll(getFilesInDirectory(mCurrentPath));
        mAdapter.notifyDataSetChanged();

        if (!new File(mCurrentPath).isFile())
            for (int i = 0; i < mListViewDirs.getCount(); i++)
                mListViewDirs.setItemChecked(i, false);
    }

    /**
     * Get readable directory name
     */
    private String getDirectoryName() {
        return mCurrentPath.substring(mCurrentPath.lastIndexOf("/"));
    }

    /**
     * Get all files ascending in current directory
     *
     * @param dir
     * Target directory
     */
    private ArrayList<String> getFilesInDirectory(String dir) {
        ArrayList<String> dirs = new ArrayList<>();

        try {
            File currentDir = new File(dir);

            if (!dir.equals(mRootPath))
                dirs.add("..");

            if (!currentDir.exists() || !currentDir.isDirectory())
                return dirs;

            for (File file : currentDir.listFiles()) {
                if (!mShowHidden && file.isHidden())
                    continue;

                if (file.isDirectory())
                    dirs.add(file.getName() + "/");
                else
                    dirs.add(file.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(dirs, new Comparator<String>(){
            public int compare(String s1, String s2){
                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }});

        return dirs;
    }

    /**
     * Set callback for file, directory or nothing selected
     *
     * @param sfc
     * Listener
     */
    public void setOnChosenListener(FileChooserListener sfc) {
        mSFCListener = sfc;
    }

    /**
     * Show/hide hidden files/directories<br>
     * Default = true
     *
     * @param showHidden
     * Show or not
     */
    public void setShowHidden(boolean showHidden) {
        this.mShowHidden = showHidden;
    }

    /**
     * Set default root directory path<br>
     * Default = external storage directory
     *
     * @param rootPath
     * Root path
     */
    public void setRootPath(String rootPath) {
        this.mRootPath = rootPath;
    }
}
