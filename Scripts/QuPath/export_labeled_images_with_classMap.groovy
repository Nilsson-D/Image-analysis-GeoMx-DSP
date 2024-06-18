def imageData = getCurrentImageData()
def server = imageData.getServer()

// Script for exporting images with labeled image (segmented image) and class map (by Pete Bankhead with minor changes)
// Define output path (relative to project)
// Check https://forum.image.sc/t/stardist-cell-classification-training-data/54217 for more info on script.

def outputDir = buildFilePath(PROJECT_BASE_DIR, 'export')
mkdirs(outputDir)
def name = GeneralTools.getNameWithoutExtension(imageData.getServer().getMetadata().getName())

// Define how much to downsample during export (may be required for large images)
double downsample = 1

// Create an ImageServer where the pixels are derived from annotations
def labelServer = new LabeledImageServer.Builder(imageData)
  .backgroundLabel(0, ColorTools.WHITE) // Specify background label (usually 0 or 255)
  .downsample(downsample)    // Choose server resolution; this should match the resolution at which tiles are exported
  .useInstanceLabels()         // Assign labels based on instances, not classifications
  .multichannelOutput(false) // If true, each label refers to the channel of a multichannel binary image (required for multiclass probability)
  .useFilter({a -> a.hasROI() && a.isDetection() == true && a.getParent().isTMACore() == false}) // Use only classified objects (customize if needed!)
  .build()
  
// Get the annotations to export - here, I assume all unclassified annotations define regions to export
def parentAnnotations = getAnnotationObjects().findAll {it.getPathClass() == null}
int count = 0
for (annotation in parentAnnotations) {
    count++
    def region = RegionRequest.createInstance(server.getPath(), downsample, annotation.getROI())
    def regionName = "${name}-${count}"
    def path = buildFilePath(outputDir, regionName + ".tiff")
    writeImageRegion(server, region, path)
    def pathLabels = buildFilePath(outputDir, regionName + "-labels.tiff")
    writeImageRegion(labelServer, region, pathLabels)
}  
  
// Try to export a classification map
// This should works in QuPath v0.2.3 - but accesses private field, may fail in later versions!
// (But later versions may also provide a much less awkward way to get the data)
def classMap = new HashMap<String, Integer>()
for (entry in labelServer.getInstanceLabels().entrySet()) {
    def label = entry.getValue().toString().replace("Label ", "") as int
    def className = entry.getKey().getPathClass().toString()
    def set = classMap.computeIfAbsent(className, e -> new TreeSet())
   set << label
}

def jsonMap = GsonTools.getInstance(true).toJson(classMap)
//println jsonMap
// Write to a file
def jsonPath = buildFilePath(outputDir, name + '-classmap.json')
new File(jsonPath).text = jsonMap