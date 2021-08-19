package org.cote.accountmanager.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MediaConverter /*extends MediaToolAdapter*/ {
	public static final Logger logger = LogManager.getLogger(MediaConverter.class);
		public MediaConverter() {
			
		}
/*	
		private IVideoResampler videoResampler = null;
	    private IAudioResampler audioResampler = null;
	    private IMediaWriter writer = null;
	    private IMediaReader reader = null;

	    @Override
	    public void onAddStream(IAddStreamEvent event) {
	        int streamIndex = event.getStreamIndex();
	        IStreamCoder streamCoder = event.getSource().getContainer().getStream(streamIndex).getStreamCoder();
	        if (streamCoder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
	            writer.addAudioStream(streamIndex, streamIndex, 2, 44100);
	        } else if (streamCoder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
	        	IVideoPicture pic = event.
	            streamCoder.setWidth(pic.getWidth());
	            streamCoder.setHeight(pic.getHeight());
	            writer.addVideoStream(streamIndex, streamIndex, pic.getWidth(), pic.getHeight());
	        }
	        super.onAddStream(event);
	    }
	*/
	    /*
	    @Override
	    public void onVideoPicture(IVideoPictureEvent event) {
	        IVideoPicture pic = event.getPicture();
	        if (videoResampler == null) {
	            videoResampler = IVideoResampler.make(pic.getWidth(), pic.getHeight(),
	                    pic.getPixelType(), pic.getWidth(), pic.getHeight(),
	                    pic.getPixelType());
	        }
	        IVideoPicture out = IVideoPicture.make(pic.getPixelType(), pic.getWidth(),
	                pic.getHeight());
	        videoResampler.resample(out, pic);

	        IVideoPictureEvent asc = new VideoPictureEvent(event.getSource(), out,
	                event.getStreamIndex());
	        super.onVideoPicture(asc);
	        out.delete();
	    }

	    @Override
	    public void onAudioSamples(IAudioSamplesEvent event) {
	        IAudioSamples samples = event.getAudioSamples();
	        if (audioResampler == null) {
	            audioResampler = IAudioResampler.make(2, samples.getChannels(),
	                    44100, samples.getSampleRate());
	        }
	        if (event.getAudioSamples().getNumSamples() > 0) {
	            IAudioSamples out = IAudioSamples.make(samples.getNumSamples(),
	                    samples.getChannels());
	            audioResampler.resample(out, samples, samples.getNumSamples());

	            AudioSamplesEvent asc = new AudioSamplesEvent(event.getSource(),
	                    out, event.getStreamIndex());
	            super.onAudioSamples(asc);
	            out.delete();
	        }
	    }
	*/
	/*
	public boolean encode(String source, String target) {
		boolean outBool = false;
		
		if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
			  throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler support) for this demo to work");

		reader = ToolFactory.makeReader(source);
		writer = ToolFactory.makeWriter(target, reader);

		reader.addListener(writer);
		IError err = null;
		while ((err = reader.readPacket()) == null);
		reader.close();
		writer.close();
		outBool = (err == null);
		if(err != null) logger.error(err.getDescription());
		return outBool;
	}
	*/
	//// https://stackoverflow.com/questions/6929349/unable-to-understand-this-method-how-does-it-try-to-match-the-frame-rate
	//// https://groups.google.com/forum/#!topic/xuggler-users/9pqji4hDwdU
	
}
