package me.sodium.graphics;

import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Informacoes da GPU e recursos OpenGL que o driver ANUNCIA (versao/extensoes).
 * Anunciar nao garante bom desempenho; modulos futuros devem ainda ter fallback.
 * Deve ser criado na thread com contexto OpenGL.
 */
public final class GraphicsCapabilities {
    public enum Vendor { NVIDIA, AMD, INTEL, MESA, OTHER }

    private static final Pattern VERSION = Pattern.compile("^(\\d+)\\.(\\d+)");
    private static final int GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX = 0x9047;

    public final String vendorString;
    public final String rendererString;
    public final String versionString;
    public final int glMajor;
    public final int glMinor;
    public final Vendor vendor;
    public final Set<String> extensions;
    /** VRAM dedicada em MiB via NVX_gpu_memory_info; -1 se indisponivel. */
    public final int dedicatedVramMiB;

    public final boolean vbo;
    public final boolean multiDrawArrays;
    public final boolean occlusionQuery;
    public final boolean vertexArrayObject;
    public final boolean mapBufferRange;
    public final boolean copyBuffer;
    public final boolean drawElementsBaseVertex;
    public final boolean fenceSync;
    public final boolean multiDrawIndirect;
    public final boolean bufferStorage;

    private GraphicsCapabilities(String vendorString, String rendererString, String versionString,
                                 int major, int minor, Set<String> ext, int vram) {
        this.vendorString = vendorString;
        this.rendererString = rendererString;
        this.versionString = versionString;
        this.glMajor = major;
        this.glMinor = minor;
        this.extensions = Collections.unmodifiableSet(ext);
        this.dedicatedVramMiB = vram;
        this.vendor = classify(vendorString, rendererString);

        this.vbo = atLeast(1, 5) || ext.contains("GL_ARB_vertex_buffer_object");
        this.multiDrawArrays = atLeast(1, 4);
        this.occlusionQuery = atLeast(1, 5) || ext.contains("GL_ARB_occlusion_query");
        this.vertexArrayObject = atLeast(3, 0) || ext.contains("GL_ARB_vertex_array_object");
        this.mapBufferRange = atLeast(3, 0) || ext.contains("GL_ARB_map_buffer_range");
        this.copyBuffer = atLeast(3, 1) || ext.contains("GL_ARB_copy_buffer");
        this.drawElementsBaseVertex = atLeast(3, 2) || ext.contains("GL_ARB_draw_elements_base_vertex");
        this.fenceSync = atLeast(3, 2) || ext.contains("GL_ARB_sync");
        this.multiDrawIndirect = atLeast(4, 3) || ext.contains("GL_ARB_multi_draw_indirect");
        this.bufferStorage = atLeast(4, 4) || ext.contains("GL_ARB_buffer_storage");
    }

    /** @throws IllegalStateException se nao houver contexto OpenGL ou a versao nao puder ser lida. */
    public static GraphicsCapabilities query() {
        String version = GL11.glGetString(GL11.GL_VERSION);
        if (version == null) {
            throw new IllegalStateException("glGetString(GL_VERSION) retornou null: contexto OpenGL indisponivel");
        }
        Matcher m = VERSION.matcher(version.trim());
        if (!m.find()) {
            throw new IllegalStateException("Formato de GL_VERSION nao reconhecido: " + version);
        }
        int major = Integer.parseInt(m.group(1));
        int minor = Integer.parseInt(m.group(2));

        String vendor = nonNull(GL11.glGetString(GL11.GL_VENDOR));
        String renderer = nonNull(GL11.glGetString(GL11.GL_RENDERER));

        Set<String> ext = new HashSet<String>();
        String extString = GL11.glGetString(GL11.GL_EXTENSIONS);
        if (extString != null) {
            for (String s : extString.split("\\s+")) {
                if (!s.isEmpty()) {
                    ext.add(s);
                }
            }
        }

        int vram = -1;
        if (ext.contains("GL_NVX_gpu_memory_info")) {
            int kib = GL11.glGetInteger(GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX);
            if (GL11.glGetError() == GL11.GL_NO_ERROR && kib > 0) {
                vram = kib / 1024;
            }
        }
        return new GraphicsCapabilities(vendor, renderer, version.trim(), major, minor, ext, vram);
    }

    public boolean atLeast(int major, int minor) {
        return glMajor > major || (glMajor == major && glMinor >= minor);
    }

    private static String nonNull(String s) {
        return s == null ? "(desconhecido)" : s;
    }

    private static Vendor classify(String vendor, String renderer) {
        String v = (vendor + " " + renderer).toLowerCase(Locale.ROOT);
        if (v.contains("mesa") || v.contains("llvmpipe")) return Vendor.MESA;
        if (v.contains("nvidia")) return Vendor.NVIDIA;
        if (v.contains("amd") || v.contains("ati ") || v.contains("radeon")) return Vendor.AMD;
        if (v.contains("intel")) return Vendor.INTEL;
        return Vendor.OTHER;
    }
}
