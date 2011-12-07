import grails.plugin.extproc.*

class BootStrap {

    def init = { servletContext ->
		
		def EXTERNAL_PROCESS_GNUPLOT = "gnuplot"
		
		String gnuplotBin = "/usr/bin/gnuplot"
		def useLocal = new File(gnuplotBin).exists() && !System.properties.forceRemote
						
		// use it
		if (useLocal) {
			log.info "using local gnuplot process"
			def gnuplot = new ExternalProcess(
				name:EXTERNAL_PROCESS_GNUPLOT,
				command:gnuplotBin,
				defaultParams:[ExternalProcess.WORKDIR_PLACEHOLDER],
				workDir:ExternalProcess.NEW_WORKDIR,
				isRemote:false,
				exposeViaWS:true,	// allow remote access
				cleanUpWorkDir:true,
				returnZippedDir:true,
				timeout:15000,
				allowedFilesPattern:'.*dat$',
				allowedFiles:["gnuplot.dat"],
				requiredFiles:[],
				returnFilesPattern:'.*\\.png$',
				returnFiles:["gnuplotted.png"]
			)
			gnuplot.save(failOnError:true,flush:true)
		}
		else {
			// use gnuplot remotely
			log.info "using remote gnuplot process"
			def gnuplotRemote = new ExternalProcess(
				name:EXTERNAL_PROCESS_GNUPLOT,
				command:"$EXTERNAL_PROCESS_GNUPLOT@http://localhost:8080/ext-proc-demo/services/externalProcess?wsdl", // TODO: CHANGEME
				workDir:"",
				isRemote:true,
				cleanUpWorkDir:true,
				returnZippedDir:true,
				timeout:15000
			)
			gnuplotRemote.save(failOnError:true,flush:true)
		}
		
		
    }
    def destroy = {
    }
}
