
(function () {
    Hemi.include("hemi.event");
    Hemi.include("hemi.data.io");
    Hemi.include("hemi.data.io.proxy");
    Hemi.namespace("uwm.proxy.provider", Hemi, {
        dispatcher: function (sProto, fHandler) {
            var t = this;
            if(!sProto) sProto = "unknown:";
            Hemi.prepareObject("uwm_proxy_provider", "%FILE_VERSION%", 1, t, 1);
            Hemi.util.logger.addLogger(t, "UWM Proxy Provider", "UWM Proxy Provider", "663");
            this.properties.protocol = sProto;
            this.objects.handler = fHandler;

            t.handle_io_register = function (oService) {

            };

            /*
            This proxy implementation works for HTTP/S GET and POST, but only the contexPath and data is being passed through
            */
            t.handle_proxy_xml = function (p, i, x, d, t, bt) {
                var oD = 0,
					sA = "Unknown",
					sN = 0,
					sId = 0,
					bFull = 1,
					sFile = 0
				;
                
                var oRequest = Hemi.data.io.service.newIORequest(
					bt,
					"UWMProxy",
					p,
					0,
					sA,
					sId,
					sN,
					(bFull ? 0 : 1),
					0,
					0,
					0
				);
                if(d) oRequest.requestData.push(d);
                oRequest.mimeType = (t ? "text/javascript" : "text/xml");
                this.log("Composed proxy request for " + p);
                return oRequest;

            };

            t.handle_io_request = function (oService, oSubject, oRequest, oResponse) {
                this.getProperties().request = 1;
                oResponse.status = true;
                var data;
                if(t.objects.handler) data = t.objects.handler(oRequest);
                if(data){
                	var repData = Hemi.data.io.service.newData();
                	repData.value = data;
                	oResponse.writeData(repData, t, 0, Hemi.data.io.service.getBusType().OFFLINE);
                }
                return 1;
            };
            
            t.ready_state = 4;
            Hemi.data.io.service.register(t, Hemi.data.io.service.getBusType().OFFLINE, 0, sProto);
            t.joinTransactionPacket("iobus");
            if(Hemi.data.io.proxy.service.properties.busType == 0){
            	t.serveTransaction("change_bus", Hemi.data.io.service.getBusType().OFFLINE);
            }
        }
          
    }, 1);
} ());
