package iped.parsers.mft;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Set;
import java.util.TimeZone;

import org.apache.tika.config.Field;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.ParsingEmbeddedDocumentExtractor;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import iped.parsers.util.Messages;
import iped.properties.ExtraProperties;
import iped.utils.LocalizedFormat;

public class MFTEntryParser extends AbstractParser {
    private static final long serialVersionUID = -9207387811762742286L;
    private static final Set<MediaType> SUPPORTED_TYPES = Collections.singleton(MediaType.parse(MFTEntry.MIME_TYPE));

    private boolean extractResidentFiles = false;
    private boolean extractNonResidentFiles = false;
    private long nonResidentFilesMaxLength = -1;

    @Field
    public void setExtractResidentFiles(boolean extractResidentFiles) {
        this.extractResidentFiles = extractResidentFiles;
    }

    @Field
    public void setExtractNonResidentFiles(boolean extractNonResidentFiles) {
        this.extractNonResidentFiles = extractNonResidentFiles;
    }

    @Field
    public void setNonResidentFilesMaxLength(long nonResidentFilesMaxLength) {
        this.nonResidentFilesMaxLength = nonResidentFilesMaxLength;
    }

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext arg0) {
        return SUPPORTED_TYPES;
    }

    @Override
    public void parse(InputStream is, ContentHandler handler, Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {
        final DateFormat df = new SimpleDateFormat(Messages.getString("DateFormat"));
        df.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        metadata.set(HttpHeaders.CONTENT_TYPE, MFTEntry.MIME_TYPE);
        metadata.remove(TikaCoreProperties.RESOURCE_NAME_KEY);

        byte[] bytes = new byte[MFTEntry.entryLength];
        int read = 0;
        while (read < MFTEntry.entryLength) {
            int r = is.read(bytes, read, MFTEntry.entryLength - read);
            if (r == -1) {
                break;
            }
            read += r;
        }
        if (read != MFTEntry.entryLength) {
            throw new TikaException("Incorrect number of bytes read from MFT entry: " + read);
        }
        MFTEntry entry = MFTEntry.parse(bytes);
        if (entry == null) {
            throw new TikaException("Invalid MFT entry.");
        }

        XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
        xhtml.startDocument();

        if (extractResidentFiles && entry.hasResidentContent() && entry.isFile()) {
            EmbeddedDocumentExtractor extractor = context.get(EmbeddedDocumentExtractor.class,
                    new ParsingEmbeddedDocumentExtractor(context));
            createResidentSubitem(handler, extractor, entry, bytes, metadata);
        }

        xhtml.startElement("style");
        xhtml.characters(
                ".tab {border-collapse: collapse; font-family: Arial, sans-serif; margin-right: 32px; margin-bottom: 32px; } "
                        + ".prop { border: solid; border-width: thin; padding: 3px; text-align: left; background-color:#EEEEEE; vertical-align: middle; } "
                        + ".val { border: solid; border-width: thin; padding: 3px; text-align: left; vertical-align: middle; } ");
        xhtml.endElement("style");
        xhtml.newline();
        xhtml.startElement("table", "class", "tab");
        if (entry.getName() != null) {
            add(xhtml, Messages.getString("MFTEntryParser.Name"), entry.getName());
        }
        if (entry.getLength() >= 0) {
            add(xhtml, Messages.getString("MFTEntryParser.Length"), LocalizedFormat.format(entry.getLength()));
        }
        if (entry.getCreationDate() != null) {
            add(xhtml, Messages.getString("MFTEntryParser.CreationDate"), df.format(entry.getCreationDate()));
        }
        if (entry.getLastModificationDate() != null) {
            add(xhtml, Messages.getString("MFTEntryParser.ModificationDate"),
                    df.format(entry.getLastModificationDate()));
        }
        if (entry.getLastAccessDate() != null) {
            add(xhtml, Messages.getString("MFTEntryParser.AccessDate"), df.format(entry.getLastAccessDate()));
        }
        if (entry.getLastEntryModificationDate() != null) {
            add(xhtml, Messages.getString("MFTEntryParser.EntryModificationDate"),
                    df.format(entry.getLastEntryModificationDate()));
        }
        if (entry.isActive()) {
            add(xhtml, Messages.getString("MFTEntryParser.Active"), Messages.getString("MFTEntryParser.Yes"));
        } else if (entry.isInactive()) {
            add(xhtml, Messages.getString("MFTEntryParser.Active"), Messages.getString("MFTEntryParser.Yes"));
        }
        if (entry.isFile()) {
            add(xhtml, Messages.getString("MFTEntryParser.Type"), Messages.getString("MFTEntryParser.File"));
        } else if (entry.isFolder()) {
            add(xhtml, Messages.getString("MFTEntryParser.Type"), Messages.getString("MFTEntryParser.Folder"));
        }
        add(xhtml, Messages.getString("MFTEntryParser.ResidentContent"),
                entry.hasResidentContent() ? Messages.getString("MFTEntryParser.Yes")
                        : Messages.getString("MFTEntryParser.No"));
        //TODO: For now, just output raw data runs values
        if (entry.getDataruns() != null) {
            add(xhtml, "Dataruns", entry.getDataruns().toString());
        }
        xhtml.endElement("table");
        xhtml.endDocument();
    }

    private void add(XHTMLContentHandler xhtml, String prop, String val) throws SAXException {
        xhtml.startElement("tr");
        xhtml.startElement("td", "class", "prop");
        xhtml.characters(prop);
        xhtml.endElement("td");
        xhtml.startElement("td", "class", "val");
        xhtml.characters(val);
        xhtml.endElement("td");
        xhtml.endElement("tr");
        xhtml.newline();
    }

    private void createResidentSubitem(ContentHandler handler, EmbeddedDocumentExtractor extractor, MFTEntry entry,
            byte[] bytes, Metadata parentMetadata) throws SAXException, IOException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, entry.getName());
        metadata.set(TikaCoreProperties.CREATED, entry.getCreationDate());
        metadata.set(TikaCoreProperties.MODIFIED, entry.getLastModificationDate());
        metadata.set(ExtraProperties.ACCESSED, entry.getLastAccessDate());
        if (entry.isInactive()) {
            metadata.set(ExtraProperties.DELETED, "true");
        }
        String parentCarvedBy = parentMetadata.get(ExtraProperties.CARVEDBY_METADATA_NAME);
        if (parentCarvedBy != null) {
            metadata.set(ExtraProperties.CARVED, "true");
            metadata.set(ExtraProperties.CARVEDBY_METADATA_NAME, parentCarvedBy);
        }
        byte[] content = entry.getResidentContent(bytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        extractor.parseEmbedded(bais, handler, metadata, false);
    }
}
