/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MimeUtil {
	public static final Logger logger = LogManager.getLogger(MimeUtil.class);
	public static String getType(String ext){
		String sLow = ext.toLowerCase();
		if(sLow.indexOf(".") > -1) sLow = sLow.substring(sLow.lastIndexOf(".") + 1,sLow.length());
		if(types.containsKey(sLow)){
			//System.out.println("Matched: '" + sLow + "'");
			return types.get(sLow);
		}
		logger.warn("No match: '" + sLow + "'");
		return null;
	}
	private static Map<String,String> types = null;
	static
	    {
		types = new HashMap<String,String>();
		types.put("weba", "audio/weba");
		types.put("webm", "video/webm");
		types.put("webp", "video/webp");
		types.put("wmv", "video/x-ms-wmv");
		types.put("flv", "video/x-flv");
		types.put("mp4", "video/mp4");
		types.put("3dm","x-world/x-3dmf");
		types.put("3dmf","x-world/x-3dmf");
		types.put("3gp","video/3gpp");
		types.put("a","application/octet-stream");
		types.put("aab","application/x-authorware-bin");
		types.put("aam","application/x-authorware-map");
		types.put("aas","application/x-authorware-seg");
		types.put("abc","text/vnd.abc");
		types.put("acgi","text/html");
		types.put("afl","video/animaflex");
		types.put("ai","application/postscript");
		types.put("aif","audio/aiff");
		types.put("aif","audio/x-aiff");
		types.put("aifc","audio/aiff");
		types.put("aifc","audio/x-aiff");
		types.put("aiff","audio/aiff");
		types.put("aiff","audio/x-aiff");
		types.put("aim","application/x-aim");
		types.put("aip","text/x-audiosoft-intra");
		types.put("ani","application/x-navi-animation");
		types.put("aos","application/x-nokia-9000-communicator-add-on-software");
		types.put("aps","application/mime");
		types.put("arc","application/octet-stream");
		types.put("arj","application/arj");
		types.put("arj","application/octet-stream");
		types.put("art","image/x-jg");
		types.put("asf","video/x-ms-asf");
		types.put("asm","text/x-asm");
		types.put("asp","text/asp");
		types.put("asx","application/x-mplayer2");
		types.put("asx","video/x-ms-asf");
		types.put("asx","video/x-ms-asf-plugin");
		types.put("au","audio/basic");
		types.put("au","audio/x-au");
		types.put("avi","application/x-troff-msvideo");
		types.put("avi","video/avi");
		types.put("avi","video/msvideo");
		types.put("avi","video/x-msvideo");
		types.put("avs","video/avs-video");
		types.put("bcpio","application/x-bcpio");
		types.put("bin","application/mac-binary");
		types.put("bin","application/macbinary");
		types.put("bin","application/octet-stream");
		types.put("bin","application/x-binary");
		types.put("bin","application/x-macbinary");
		types.put("bm","image/bmp");
		types.put("bmp","image/bmp");
		types.put("bmp","image/x-windows-bmp");
		types.put("boo","application/book");
		types.put("book","application/book");
		types.put("boz","application/x-bzip2");
		types.put("bsh","application/x-bsh");
		types.put("bz","application/x-bzip");
		types.put("bz2","application/x-bzip2");
		types.put("c","text/plain");
		types.put("c","text/x-c");
		types.put("c++","text/plain");
		types.put("cat","application/vnd.ms-pki.seccat");
		types.put("cc","text/plain");
		types.put("cc","text/x-c");
		types.put("ccad","application/clariscad");
		types.put("cco","application/x-cocoa");
		types.put("cdf","application/cdf");
		types.put("cdf","application/x-cdf");
		types.put("cdf","application/x-netcdf");
		types.put("cer","application/pkix-cert");
		types.put("cer","application/x-x509-ca-cert");
		types.put("cha","application/x-chat");
		types.put("chat","application/x-chat");
		types.put("class","application/java");
		types.put("class","application/java-byte-code");
		types.put("class","application/x-java-class");
		types.put("com","application/octet-stream");
		types.put("com","text/plain");
		types.put("conf","text/plain");
		types.put("cpio","application/x-cpio");
		types.put("cpp","text/x-c");
		types.put("cpt","application/mac-compactpro");
		types.put("cpt","application/x-compactpro");
		types.put("cpt","application/x-cpt");
		types.put("crl","application/pkcs-crl");
		types.put("crl","application/pkix-crl");
		types.put("crt","application/pkix-cert");
		types.put("crt","application/x-x509-ca-cert");
		types.put("crt","application/x-x509-user-cert");
		types.put("csh","application/x-csh");
		types.put("csh","text/x-script.csh");
		types.put("css","application/x-pointplus");
		types.put("css","text/css");
		types.put("csv","text/csv");
		types.put("cxx","text/plain");
		types.put("dcr","application/x-director");
		types.put("deepv","application/x-deepv");
		types.put("def","text/plain");
		types.put("der","application/x-x509-ca-cert");
		types.put("dif","video/x-dv");
		types.put("dir","application/x-director");
		types.put("dl","video/dl");
		types.put("dl","video/x-dl");
		types.put("doc","application/msword");
		types.put("dot","application/msword");
		types.put("dp","application/commonground");
		types.put("drw","application/drafting");
		types.put("dump","application/octet-stream");
		types.put("dv","video/x-dv");
		types.put("dvi","application/x-dvi");
		types.put("dwf","drawing/x-dwf (old)");
		types.put("dwf","model/vnd.dwf");
		types.put("dwg","application/acad");
		types.put("dwg","image/vnd.dwg");
		types.put("dwg","image/x-dwg");
		types.put("dxf","application/dxf");
		types.put("dxf","image/vnd.dwg");
		types.put("dxf","image/x-dwg");
		types.put("dxr","application/x-director");
		types.put("el","text/x-script.elisp");
		types.put("elc","application/x-bytecode.elisp (compiled elisp)");
		types.put("elc","application/x-elc");
		types.put("env","application/x-envoy");
		types.put("eps","application/postscript");
		types.put("es","application/x-esrehber");
		types.put("etx","text/x-setext");
		types.put("evy","application/envoy");
		types.put("evy","application/x-envoy");
		types.put("exe","application/octet-stream");
		types.put("f","text/plain");
		types.put("f","text/x-fortran");
		types.put("f77","text/x-fortran");
		types.put("f90","text/plain");
		types.put("f90","text/x-fortran");
		types.put("fdf","application/vnd.fdf");
		types.put("fif","application/fractals");
		types.put("fif","image/fif");
		types.put("fli","video/fli");
		types.put("fli","video/x-fli");
		types.put("flo","image/florian");
		types.put("flx","text/vnd.fmi.flexstor");
		types.put("fmf","video/x-atomic3d-feature");
		types.put("for","text/plain");
		types.put("for","text/x-fortran");
		types.put("fpx","image/vnd.fpx");
		types.put("fpx","image/vnd.net-fpx");
		types.put("frl","application/freeloader");
		types.put("funk","audio/make");
		types.put("g","text/plain");
		types.put("g3","image/g3fax");
		types.put("gif","image/gif");
		types.put("gl","video/gl");
		types.put("gl","video/x-gl");
		types.put("gsd","audio/x-gsm");
		types.put("gsm","audio/x-gsm");
		types.put("gsp","application/x-gsp");
		types.put("gss","application/x-gss");
		types.put("gtar","application/x-gtar");
		types.put("gz","application/x-compressed");
		types.put("gz","application/x-gzip");
		types.put("gzip","application/x-gzip");
		types.put("gzip","multipart/x-gzip");
		types.put("h","text/plain");
		types.put("h","text/x-h");
		types.put("hdf","application/x-hdf");
		types.put("help","application/x-helpfile");
		types.put("hgl","application/vnd.hp-hpgl");
		types.put("hh","text/plain");
		types.put("hh","text/x-h");
		types.put("hlb","text/x-script");
		types.put("hlp","application/hlp");
		types.put("hlp","application/x-helpfile");
		types.put("hlp","application/x-winhelp");
		types.put("hpg","application/vnd.hp-hpgl");
		types.put("hpgl","application/vnd.hp-hpgl");
		types.put("hqx","application/binhex");
		types.put("hqx","application/binhex4");
		types.put("hqx","application/mac-binhex");
		types.put("hqx","application/mac-binhex40");
		types.put("hqx","application/x-binhex40");
		types.put("hqx","application/x-mac-binhex40");
		types.put("hta","application/hta");
		types.put("htc","text/x-component");
		types.put("htm","text/html");
		types.put("html","text/html");
		types.put("htmls","text/html");
		types.put("htt","text/webviewhtml");
		types.put("htx","text/html");
		types.put("ice","x-conference/x-cooltalk");
		types.put("ico","image/x-icon");
		types.put("idc","text/plain");
		types.put("ief","image/ief");
		types.put("iefs","image/ief");
		types.put("iges","application/iges");
		types.put("iges","model/iges");
		types.put("igs","application/iges");
		types.put("igs","model/iges");
		types.put("ima","application/x-ima");
		types.put("imap","application/x-httpd-imap");
		types.put("inf","application/inf");
		types.put("ins","application/x-internett-signup");
		types.put("ip","application/x-ip2");
		types.put("isu","video/x-isvideo");
		types.put("it","audio/it");
		types.put("iv","application/x-inventor");
		types.put("ivr","i-world/i-vrml");
		types.put("ivy","application/x-livescreen");
		types.put("jam","audio/x-jam");
		types.put("jav","text/plain");
		types.put("jav","text/x-java-source");
		types.put("java","text/plain");
		types.put("java","text/x-java-source");
		types.put("jcm","application/x-java-commerce");
		types.put("jfif","image/jpeg");
		types.put("jfif","image/pjpeg");
		types.put("jfif-tbnl","image/jpeg");
		types.put("jpe","image/jpeg");
		types.put("jpe","image/pjpeg");
		types.put("jpeg","image/jpeg");
		types.put("jpeg","image/pjpeg");
		types.put("jpg","image/jpeg");
		//types.put("jpg","image/pjpeg");
		types.put("jps","image/x-jps");
		types.put("js","application/x-javascript");
		types.put("jut","image/jutvision");
		types.put("kar","audio/midi");
		types.put("kar","music/x-karaoke");
		types.put("ksh","application/x-ksh");
		types.put("ksh","text/x-script.ksh");
		types.put("la","audio/nspaudio");
		types.put("la","audio/x-nspaudio");
		types.put("lam","audio/x-liveaudio");
		types.put("latex","application/x-latex");
		types.put("lha","application/lha");
		types.put("lha","application/octet-stream");
		types.put("lha","application/x-lha");
		types.put("lhx","application/octet-stream");
		types.put("list","text/plain");
		types.put("lma","audio/nspaudio");
		types.put("lma","audio/x-nspaudio");
		types.put("log","text/plain");
		types.put("lsp","application/x-lisp");
		types.put("lsp","text/x-script.lisp");
		types.put("lst","text/plain");
		types.put("lsx","text/x-la-asf");
		types.put("ltx","application/x-latex");
		types.put("lzh","application/octet-stream");
		types.put("lzh","application/x-lzh");
		types.put("lzx","application/lzx");
		types.put("lzx","application/octet-stream");
		types.put("lzx","application/x-lzx");
		types.put("m","text/plain");
		types.put("m","text/x-m");
		types.put("m1v","video/mpeg");
		types.put("m2a","audio/mpeg");
		types.put("m2v","video/mpeg");
		types.put("m3u","audio/x-mpequrl");
		types.put("man","application/x-troff-man");
		types.put("map","application/x-navimap");
		types.put("mar","text/plain");
		types.put("mbd","application/mbedlet");
		types.put("mc$","application/x-magic-cap-package-1.0");
		types.put("mcd","application/mcad");
		types.put("mcd","application/x-mathcad");
		types.put("mcf","image/vasa");
		types.put("mcf","text/mcf");
		types.put("mcp","application/netmc");
		types.put("me","application/x-troff-me");
		types.put("mht","message/rfc822");
		types.put("mhtml","message/rfc822");
		types.put("mid","application/x-midi");
		types.put("mid","audio/midi");
		types.put("mid","audio/x-mid");
		types.put("mid","audio/x-midi");
		types.put("mid","music/crescendo");
		types.put("mid","x-music/x-midi");
		types.put("midi","application/x-midi");
		types.put("midi","audio/midi");
		types.put("midi","audio/x-mid");
		types.put("midi","audio/x-midi");
		types.put("midi","music/crescendo");
		types.put("midi","x-music/x-midi");
		types.put("mif","application/x-frame");
		types.put("mif","application/x-mif");
		types.put("mime","message/rfc822");
		types.put("mime","www/mime");
		types.put("mjf","audio/x-vnd.audioexplosion.mjuicemediafile");
		types.put("mjpg","video/x-motion-jpeg");
		types.put("mm","application/base64");
		types.put("mm","application/x-meme");
		types.put("mme","application/base64");
		types.put("mod","audio/mod");
		types.put("mod","audio/x-mod");
		types.put("moov","video/quicktime");
		types.put("mov","video/quicktime");
		types.put("movie","video/x-sgi-movie");
		types.put("mp2","audio/mpeg");
		types.put("mp2","audio/x-mpeg");
		types.put("mp2","video/mpeg");
		types.put("mp2","video/x-mpeg");
		types.put("mp2","video/x-mpeq2a");
		types.put("mp3","audio/mpeg3");
		/*
		types.put("mp3","audio/x-mpeg-3");
		types.put("mp3","video/mpeg");
		types.put("mp3","video/x-mpeg");
		*/
		types.put("mpa","audio/mpeg");
		/// types.put("mpa","video/mpeg");
		types.put("mpc","application/x-project");
		types.put("mpe","video/mpeg");
		types.put("mpeg","video/mpeg");
		types.put("mpg","audio/mpeg");
		types.put("mpg","video/mpeg");
		types.put("mpga","audio/mpeg");
		types.put("mpp","application/vnd.ms-project");
		types.put("mpt","application/x-project");
		types.put("mpv","application/x-project");
		types.put("mpx","application/x-project");
		types.put("mrc","application/marc");
		types.put("ms","application/x-troff-ms");
		types.put("mv","video/x-sgi-movie");
		types.put("my","audio/make");
		types.put("mzz","application/x-vnd.audioexplosion.mzz");
		types.put("nap","image/naplps");
		types.put("naplps","image/naplps");
		types.put("nc","application/x-netcdf");
		types.put("ncm","application/vnd.nokia.configuration-message");
		types.put("nif","image/x-niff");
		types.put("niff","image/x-niff");
		types.put("nix","application/x-mix-transfer");
		types.put("nsc","application/x-conference");
		types.put("nvd","application/x-navidoc");
		types.put("o","application/octet-stream");
		types.put("oda","application/oda");
		types.put("omc","application/x-omc");
		types.put("omcd","application/x-omcdatamaker");
		types.put("omcr","application/x-omcregerator");
		types.put("p","text/x-pascal");
		types.put("p10","application/pkcs10");
		types.put("p10","application/x-pkcs10");
		types.put("p12","application/pkcs-12");
		types.put("p12","application/x-pkcs12");
		types.put("p7a","application/x-pkcs7-signature");
		types.put("p7c","application/pkcs7-mime");
		types.put("p7c","application/x-pkcs7-mime");
		types.put("p7m","application/pkcs7-mime");
		types.put("p7m","application/x-pkcs7-mime");
		types.put("p7r","application/x-pkcs7-certreqresp");
		types.put("p7s","application/pkcs7-signature");
		types.put("part","application/pro_eng");
		types.put("pas","text/pascal");
		types.put("pbm","image/x-portable-bitmap");
		types.put("pcl","application/vnd.hp-pcl");
		types.put("pcl","application/x-pcl");
		types.put("pct","image/x-pict");
		types.put("pcx","image/x-pcx");
		types.put("pdb","chemical/x-pdb");
		types.put("pdf","application/pdf");
		types.put("pfunk","audio/make");
		types.put("pfunk","audio/make.my.funk");
		types.put("pgm","image/x-portable-graymap");
		types.put("pgm","image/x-portable-greymap");
		types.put("pic","image/pict");
		types.put("pict","image/pict");
		types.put("pkg","application/x-newton-compatible-pkg");
		types.put("pko","application/vnd.ms-pki.pko");
		types.put("pl","text/plain");
		types.put("pl","text/x-script.perl");
		types.put("plx","application/x-pixclscript");
		types.put("pm","image/x-xpixmap");
		types.put("pm","text/x-script.perl-module");
		types.put("pm4","application/x-pagemaker");
		types.put("pm5","application/x-pagemaker");
		types.put("png","image/png");
		types.put("pnm","application/x-portable-anymap");
		types.put("pnm","image/x-portable-anymap");
		types.put("pot","application/mspowerpoint");
		types.put("pot","application/vnd.ms-powerpoint");
		types.put("pov","model/x-pov");
		types.put("ppa","application/vnd.ms-powerpoint");
		types.put("ppm","image/x-portable-pixmap");
		types.put("pps","application/mspowerpoint");
		types.put("pps","application/vnd.ms-powerpoint");
		types.put("ppt","application/mspowerpoint");
		types.put("ppt","application/powerpoint");
		types.put("ppt","application/vnd.ms-powerpoint");
		types.put("ppt","application/x-mspowerpoint");
		types.put("ppz","application/mspowerpoint");
		types.put("pre","application/x-freelance");
		types.put("prt","application/pro_eng");
		types.put("ps","application/postscript");
		types.put("psd","application/octet-stream");
		types.put("pvu","paleovu/x-pv");
		types.put("pwz","application/vnd.ms-powerpoint");
		types.put("py","text/x-script.phyton");
		types.put("pyc","applicaiton/x-bytecode.python");
		types.put("qcp","audio/vnd.qcelp");
		types.put("qd3","x-world/x-3dmf");
		types.put("qd3d","x-world/x-3dmf");
		types.put("qif","image/x-quicktime");
		types.put("qt","video/quicktime");
		types.put("qtc","video/x-qtc");
		types.put("qti","image/x-quicktime");
		types.put("qtif","image/x-quicktime");
		types.put("ra","audio/x-pn-realaudio");
		types.put("ra","audio/x-pn-realaudio-plugin");
		types.put("ra","audio/x-realaudio");
		types.put("ram","audio/x-pn-realaudio");
		types.put("ras","application/x-cmu-raster");
		types.put("ras","image/cmu-raster");
		types.put("ras","image/x-cmu-raster");
		types.put("rast","image/cmu-raster");
		types.put("rexx","text/x-script.rexx");
		types.put("rf","image/vnd.rn-realflash");
		types.put("rgb","image/x-rgb");
		types.put("rm","application/vnd.rn-realmedia");
		types.put("rm","audio/x-pn-realaudio");
		types.put("rmi","audio/mid");
		types.put("rmm","audio/x-pn-realaudio");
		types.put("rmp","audio/x-pn-realaudio");
		types.put("rmp","audio/x-pn-realaudio-plugin");
		types.put("rng","application/ringing-tones");
		types.put("rng","application/vnd.nokia.ringing-tone");
		types.put("rnx","application/vnd.rn-realplayer");
		types.put("roff","application/x-troff");
		types.put("rp","image/vnd.rn-realpix");
		types.put("rpm","audio/x-pn-realaudio-plugin");
		types.put("rt","text/richtext");
		types.put("rt","text/vnd.rn-realtext");
		types.put("rtf","application/rtf");
		types.put("rtf","application/x-rtf");
		types.put("rtf","text/richtext");
		types.put("rtx","application/rtf");
		types.put("rtx","text/richtext");
		types.put("rv","video/vnd.rn-realvideo");
		types.put("s","text/x-asm");
		types.put("s3m","audio/s3m");
		types.put("saveme","application/octet-stream");
		types.put("sbk","application/x-tbook");
		types.put("scm","application/x-lotusscreencam");
		types.put("scm","text/x-script.guile");
		types.put("scm","text/x-script.scheme");
		types.put("scm","video/x-scm");
		types.put("sdml","text/plain");
		types.put("sdp","application/sdp");
		types.put("sdp","application/x-sdp");
		types.put("sdr","application/sounder");
		types.put("sea","application/sea");
		types.put("sea","application/x-sea");
		types.put("set","application/set");
		types.put("sgm","text/sgml");
		types.put("sgm","text/x-sgml");
		types.put("sgml","text/sgml");
		types.put("sgml","text/x-sgml");
		types.put("sh","application/x-bsh");
		types.put("sh","application/x-sh");
		types.put("sh","application/x-shar");
		types.put("sh","text/x-script.sh");
		types.put("shar","application/x-bsh");
		types.put("shar","application/x-shar");
		types.put("shtml","text/html");
		types.put("shtml","text/x-server-parsed-html");
		types.put("sid","audio/x-psid");
		types.put("sit","application/x-sit");
		types.put("sit","application/x-stuffit");
		types.put("skd","application/x-koan");
		types.put("skm","application/x-koan");
		types.put("skp","application/x-koan");
		types.put("skt","application/x-koan");
		types.put("sl","application/x-seelogo");
		types.put("smi","application/smil");
		types.put("smil","application/smil");
		types.put("snd","audio/basic");
		types.put("snd","audio/x-adpcm");
		types.put("sol","application/solids");
		types.put("spc","application/x-pkcs7-certificates");
		types.put("spc","text/x-speech");
		types.put("spl","application/futuresplash");
		types.put("spr","application/x-sprite");
		types.put("sprite","application/x-sprite");
		types.put("src","application/x-wais-source");
		types.put("ssi","text/x-server-parsed-html");
		types.put("ssm","application/streamingmedia");
		types.put("sst","application/vnd.ms-pki.certstore");
		types.put("step","application/step");
		types.put("stl","application/sla");
		types.put("stl","application/vnd.ms-pki.stl");
		types.put("stl","application/x-navistyle");
		types.put("stp","application/step");
		types.put("sv4cpio","application/x-sv4cpio");
		types.put("sv4crc","application/x-sv4crc");
		types.put("svf","image/vnd.dwg");
		types.put("svf","image/x-dwg");
		types.put("svr","application/x-world");
		types.put("svr","x-world/x-svr");
		types.put("swf","application/x-shockwave-flash");
		types.put("t","application/x-troff");
		types.put("talk","text/x-speech");
		types.put("tar","application/x-tar");
		types.put("tbk","application/toolbook");
		types.put("tbk","application/x-tbook");
		types.put("tcl","application/x-tcl");
		types.put("tcl","text/x-script.tcl");
		types.put("tcsh","text/x-script.tcsh");
		types.put("tex","application/x-tex");
		types.put("texi","application/x-texinfo");
		types.put("texinfo","application/x-texinfo");
		types.put("text","application/plain");
		types.put("text","text/plain");
		types.put("tgz","application/gnutar");
		types.put("tgz","application/x-compressed");
		types.put("tif","image/tiff");
		types.put("tif","image/x-tiff");
		types.put("tiff","image/tiff");
		types.put("tiff","image/x-tiff");
		types.put("tr","application/x-troff");
		types.put("tsi","audio/tsp-audio");
		types.put("tsp","application/dsptype");
		types.put("tsp","audio/tsplayer");
		types.put("tsv","text/tab-separated-values");
		types.put("turbot","image/florian");
		types.put("txt","text/plain");
		types.put("uil","text/x-uil");
		types.put("uni","text/uri-list");
		types.put("unis","text/uri-list");
		types.put("unv","application/i-deas");
		types.put("uri","text/uri-list");
		types.put("uris","text/uri-list");
		types.put("ustar","application/x-ustar");
		types.put("ustar","multipart/x-ustar");
		types.put("uu","application/octet-stream");
		types.put("uu","text/x-uuencode");
		types.put("uue","text/x-uuencode");
		types.put("vcd","application/x-cdlink");
		types.put("vcs","text/x-vcalendar");
		types.put("vda","application/vda");
		types.put("vdo","video/vdo");
		types.put("vew","application/groupwise");
		types.put("viv","video/vivo");
		types.put("viv","video/vnd.vivo");
		types.put("vivo","video/vivo");
		types.put("vivo","video/vnd.vivo");
		types.put("vmd","application/vocaltec-media-desc");
		types.put("vmf","application/vocaltec-media-file");
		types.put("voc","audio/voc");
		types.put("voc","audio/x-voc");
		types.put("vos","video/vosaic");
		types.put("vox","audio/voxware");
		types.put("vqe","audio/x-twinvq-plugin");
		types.put("vqf","audio/x-twinvq");
		types.put("vql","audio/x-twinvq-plugin");
		types.put("vrml","application/x-vrml");
		types.put("vrml","model/vrml");
		types.put("vrml","x-world/x-vrml");
		types.put("vrt","x-world/x-vrt");
		types.put("vsd","application/x-visio");
		types.put("vst","application/x-visio");
		types.put("vsw","application/x-visio");
		types.put("w60","application/wordperfect6.0");
		types.put("w61","application/wordperfect6.1");
		types.put("w6w","application/msword");
		types.put("wav","audio/wav");
		types.put("wav","audio/x-wav");
		types.put("wb1","application/x-qpro");
		types.put("wbmp","image/vnd.wap.wbmp");
		types.put("web","application/vnd.xara");
		types.put("wiz","application/msword");
		types.put("wk1","application/x-123");
		types.put("wmf","windows/metafile");
		types.put("wml","text/vnd.wap.wml");
		types.put("wmlc","application/vnd.wap.wmlc");
		types.put("wmls","text/vnd.wap.wmlscript");
		types.put("wmlsc","application/vnd.wap.wmlscriptc");
		types.put("word","application/msword");
		types.put("wp","application/wordperfect");
		types.put("wp5","application/wordperfect");
		types.put("wp5","application/wordperfect6.0");
		types.put("wp6","application/wordperfect");
		types.put("wpd","application/wordperfect");
		types.put("wpd","application/x-wpwin");
		types.put("wq1","application/x-lotus");
		types.put("wri","application/mswrite");
		types.put("wri","application/x-wri");
		types.put("wrl","application/x-world");
		types.put("wrl","model/vrml");
		types.put("wrl","x-world/x-vrml");
		types.put("wrz","model/vrml");
		types.put("wrz","x-world/x-vrml");
		types.put("wsc","text/scriplet");
		types.put("wsrc","application/x-wais-source");
		types.put("wtk","application/x-wintalk");
		types.put("xbm","image/x-xbitmap");
		types.put("xbm","image/x-xbm");
		types.put("xbm","image/xbm");
		types.put("xdr","video/x-amt-demorun");
		types.put("xgz","xgl/drawing");
		types.put("xif","image/vnd.xiff");
		types.put("xl","application/excel");
		types.put("xla","application/excel");
		types.put("xla","application/x-excel");
		types.put("xla","application/x-msexcel");
		types.put("xlb","application/excel");
		types.put("xlb","application/vnd.ms-excel");
		types.put("xlb","application/x-excel");
		types.put("xlc","application/excel");
		types.put("xlc","application/vnd.ms-excel");
		types.put("xlc","application/x-excel");
		types.put("xld","application/excel");
		types.put("xld","application/x-excel");
		types.put("xlk","application/excel");
		types.put("xlk","application/x-excel");
		types.put("xll","application/excel");
		types.put("xll","application/vnd.ms-excel");
		types.put("xll","application/x-excel");
		types.put("xlm","application/excel");
		types.put("xlm","application/vnd.ms-excel");
		types.put("xlm","application/x-excel");
		types.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		types.put("xls","application/excel");
		types.put("xls","application/vnd.ms-excel");
		types.put("xls","application/x-excel");
		types.put("xls","application/x-msexcel");
		types.put("xlt","application/excel");
		types.put("xlt","application/x-excel");
		types.put("xlv","application/excel");
		types.put("xlv","application/x-excel");
		types.put("xlw","application/excel");
		types.put("xlw","application/vnd.ms-excel");
		types.put("xlw","application/x-excel");
		types.put("xlw","application/x-msexcel");
		types.put("xm","audio/xm");
		types.put("xml","application/xml");
		types.put("xml","text/xml");
		types.put("xmz","xgl/movie");
		types.put("xpix","application/x-vnd.ls-xpix");
		types.put("xpm","image/x-xpixmap");
		types.put("xpm","image/xpm");
		types.put("x-png","image/png");
		types.put("xsr","video/x-amt-showrun");
		types.put("xwd","image/x-xwd");
		types.put("xwd","image/x-xwindowdump");
		types.put("xyz","chemical/x-pdb");
		types.put("z","application/x-compress");
		types.put("z","application/x-compressed");
		types.put("zip","application/x-compressed");
		types.put("zip","application/x-zip-compressed");
		types.put("zip","application/zip");
		types.put("zip","multipart/x-zip");
		types.put("zoo","application/octet-stream");
		types.put("zsh","text/x-script.zsh");
	    }
}
