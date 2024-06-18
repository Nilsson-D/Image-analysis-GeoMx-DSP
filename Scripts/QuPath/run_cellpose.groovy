import qupath.ext.biop.cellpose.Cellpose2D

// Specify the model name 
pathModel = '<path to nuclei model>'

param_threshold = 0.4//threshold for detection. All cells segmented by StarDist will have a detection probability associated with it, where higher values indicate more certain detections. Floating point, range is 0 to 1. Default 0.5
param_pixelsize=.4//Pixel scale to perform segmentation at. Set to 0 for image resolution (default). Int values accepted, greater values  will be faster but may yield poorer segmentations.
param_tilesize=1024 //size of tile in pixels for processing. Must be a multiple of 16. Lower values may solve any memory-related errors, but can take longer to process. Default is 1024.
normalize_low_pct=0.0 //lower limit for normalization. Set to 0 to disable
normalize_high_pct=99.8 // upper limit for normalization. Set to 100 to disable.
cellConstrainScale = 2
cellExpansion = 1.5

setChannelNames('Syto13', 'CD3', 'CD57', 'CD8') //processed

def cellpose = Cellpose2D.builder( pathModel )
        .pixelSize(param_pixelsize)              // Resolution for detection
        .channels("Syto13")
        .tileSize(param_tilesize)   
        .diameter(0)                   // Median object diameter. Set to 0.0 for the `bact_omni` model or for automatic computation
        .measureShape()                // Add shape measurements
        .measureIntensity()            // Add cell measurements (in all compartments)  
        .normalizePercentilesGlobal(normalize_low_pct, normalize_high_pct, 10)
        .cellConstrainScale(cellConstrainScale)
        .cellExpansion(cellExpansion)
        .setOverlap(200)
        .build()


// Run detection for the selected objects
imageData = getCurrentImageData()
selectTMACores()
pathObjects = getSelectedObjects()
if (pathObjects.isEmpty()) { //check if any ROI is selected
    Dialogs.showErrorMessage("Cellpose", "Please select a parent object!")
    return
}
cellpose.detectObjects(imageData, pathObjects)
println "Done!"
