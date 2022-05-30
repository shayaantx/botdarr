package com.botdarr.api.lidarr;

import java.util.ArrayList;
import java.util.List;

public class LidarrAlbum {
    public List<LidarrImage> getImages() {
        return images;
    }

    public void setImages(List<LidarrImage> images) {
        this.images = images;
    }

    private List<LidarrImage> images = new ArrayList<>();
}
