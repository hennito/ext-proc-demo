package extproc.demo

import grails.plugin.extproc.*

class InvokerController {

	def fileHandlingService
	def externalProcessService
	
	def sampleGnuPlot ="""set terminal png transparent nocrop enhanced font arial 8 size 420,320
set output 'gnuplotted.png'
set grid nopolar
set grid xtics nomxtics ytics nomytics noztics nomztics \
 nox2tics nomx2tics noy2tics nomy2tics nocbtics nomcbtics
set grid layerdefault   linetype 0 linewidth 1.000,  linetype 0 linewidth 1.000
set samples 21, 21
set isosamples 11, 11
set title "3D gnuplot demo"
set xlabel "X axis"
set xlabel  offset character -3, -2, 0 font "" textcolor lt -1 norotate
set xrange [ -10.0000 : 10.0000 ] noreverse nowriteback
set ylabel "Y axis"
set ylabel  offset character 3, -2, 0 font "" textcolor lt -1 rotate by 90
set yrange [ -10.0000 : 10.0000 ] noreverse nowriteback
set zlabel "Z axis"
set zlabel  offset character -5, 0, 0 font "" textcolor lt -1 norotate
splot x**2+y**2, x**2-y**2
"""

    def index() { }
	
	def invoke = {
		def sampleInput = params.sampleInput ?: sampleGnuPlot
	
// create temp dir with the gnuplot input file
		def tempDir = fileHandlingService.createTempDir()
		def inputFile = fileHandlingService.fileInTemp(tempDir, "gnuplot.dat")
		inputFile << sampleInput

// create the input for the external process
		ExternalProcessInput input = new ExternalProcessInput()
		input.parameters =  ["gnuplot.dat"]
		input.zippedWorkDir = fileHandlingService.zipDir(tempDir) { fn -> "gnuplot.dat".equals(fn)}
		
// invoke the external process (never mind if it's local or remote
		ExternalProcessResult output = externalProcessService.executeProcess("gnuplot", input)

// check for errors
		if (output.serviceReturn != null) {
			// we have a ext proc error
			log.error "EXT-PROC error ${output.serviceReturn}"
			render "EXT-PROC error\n${output.serviceReturn}"
		} else {
			if (output.returnCode != 0) {
				// 	we have a gnuplot error
				log.error "GNUPLOT error ${output.consoleLog}"
				render "GNUPLOT error\n ${output.consoleLog}"
			}
			else {
				// all set, we got the response
				fileHandlingService.unzipByteArrayToDir(output.zippedDir, tempDir) { fn -> "gnuplotted.png".equals(fn)}
				// no we have the result file from gnuplot in the local temp dir
				File resultFile = fileHandlingService.fileInTemp(tempDir,"gnuplotted.png")
				byte[] content = resultFile.bytes
				def fileSize = content.length
				response.setContentType("image/png")
				response.setContentLength(fileSize)
				def out = response.outputStream
				out << content
				out.close()
			}
		}
	}

	
}
