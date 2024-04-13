package android21ktpm3.group07.androidgallery.ui.memories;

import static android.media.MediaRecorder.VideoEncoder.AV1;
import static android.media.MediaRecorder.VideoEncoder.H263;
import static android.media.MediaRecorder.VideoEncoder.HEVC;
import static android.media.MediaRecorder.VideoEncoder.MPEG_4_SP;


import android.graphics.Bitmap;
import android.media.MediaRecorder;

import androidx.media3.extractor.AvcConfig;

import org.jcodec.api.SequenceEncoder;


import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.codecs.h264.mp4.AvcCBox;
import org.jcodec.common.Codec;
import org.jcodec.common.Format;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.jcodec.containers.mp4.boxes.AVC1Box;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;



public class JCodecSequenceEncoder {
    private final SequenceEncoder encoder;

    public JCodecSequenceEncoder(File out) throws IOException {

        FileChannel channel = FileChannel.open(out.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        SeekableByteChannel outChannel = NIOUtils.writableChannel(out);


        // Khởi tạo encoder với tệp đầu ra
        encoder = new AndroidSequenceEncoder(outChannel,Rational.R(25,1));
    }

    public void encodeBitmaps(Bitmap[] bitmaps) throws IOException {
        for (Bitmap bitmap : bitmaps) {

            Picture picture = fromBitmap(bitmap);
            System.out.println(picture);
            encoder.encodeNativeFrame(picture);
        }
    }

    private Picture fromBitmap(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        // Khởi tạo ByteBuffer và sao chép dữ liệu từ mảng pixels vào đó
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        for (int pixel : pixels) {
            buffer.putInt(pixel);
        }

        // Tạo Picture với dữ liệu RGB từ ByteBuffer
        Picture picture = Picture.create(width, height, ColorSpace.RGB);
       // picture.setCrop(0, 0, width, height);
       // picture.setData(buffer.array());

        return picture;
    }

    public void finish() throws IOException {
        // Kết thúc quá trình mã hóa
        encoder.finish();
    }
}