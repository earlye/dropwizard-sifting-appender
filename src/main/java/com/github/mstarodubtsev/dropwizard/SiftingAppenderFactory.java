package com.github.mstarodubtsev.dropwizard;

import java.nio.file.Paths;
import java.util.TimeZone;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.sift.MDCBasedDiscriminator;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.sift.AppenderFactory;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.dropwizard.validation.ValidationMethod;

/**
 * An {@link AppenderFactory} implementation which provides an appender that splits events to separate
 * log files depending on MDC context.
 * <b>Configuration Parameters:</b>
 * <table summary="">
 *     <tr>
 *         <th class="head">Name</th>
 *         <th class="head">Default</th>
 *         <th class="head">Description</th>
 *     </tr>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code type}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>The appender type. Must be {@code sift}.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code threshold}</td>
 *         <td>{@code ALL}</td>
 *         <td>The lowest level of events to processing by appender.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code timeZone}</td>
 *         <td>{@code UTC}</td>
 *         <td>The time zone to which event timestamps will be converted.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code discriminatorKey}</td>
 *         <td>{@code logfileName}</td>
 *         <td>Discriminator key for sift events</td>
 *     </tr>
 *     <tr>
 *         <td>{@code discriminatorDefaultValue}</td>
 *         <td>{@code logfileName}</td>
 *         <td>Discriminator default value</td>
 *     </tr>
 *     <tr>
 *         <td>{@code loggindDirectory}</td>
 *         <td>{@code <i>none</i>}</td>
 *         <td>Directory to place log files in.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code messagePattern}</td>
 *         <td>the default format</td>
 *         <td>
 *             The Logback pattern with which events will be formatted. See
 *             <a href="http://logback.qos.ch/manual/layouts.html#conversionWord">the Logback documentation</a>
 *             for details.
 *         </td>
 *     </tr>
 * </table>
 *
 * @param <E> - parameter to pass down to AbstractAppenderFactory
 *
 * @see AbstractAppenderFactory
 */
@JsonTypeName("sift")
public class SiftingAppenderFactory<E extends DeferredProcessingAware> extends AbstractAppenderFactory<E> {
    /**
     * Discriminator key for sift events.
     */
    @NotNull
    private String discriminatorKey;

    /**
     * Discriminator default value.
     */
    @NotNull
    private String discriminatorDefaultValue;

    /**
     * Logging directory.
     */
    @NotNull
    private String loggingDirectory;

    /**
     * The Logback pattern with which events will be formatted.
     */
    @NotNull
    private String messagePattern;

    /**
     * The time zone to which event timestamps will be converted.
     */
    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    /**
     * Discriminator key getter.
     * @return Discriminator key
     */
    public final String getDiscriminatorKey() {
        return discriminatorKey;
    }

    /**
     * Discriminator key setter.
     * @param discriminatorKey Discriminator key
     */
    public final void setDiscriminatorKey(final String discriminatorKey) {
        this.discriminatorKey = discriminatorKey;
    }

    /**
     * Discriminator default value getter.
     * @return Discriminator default value
     */
    public final String getDiscriminatorDefaultValue() {
        return discriminatorDefaultValue;
    }

    /**
     * Discriminator default value setter.
     * @param discriminatorDefaultValue Discriminator default value
     */
    public final void setDiscriminatorDefaultValue(final String discriminatorDefaultValue) {
        this.discriminatorDefaultValue = discriminatorDefaultValue;
    }

    /**
     * The message pattern getter.
     * @return Message pattern
     */
    public final String getMessagePattern() {
        return messagePattern;
    }

    /**
     * The message pattern setter.
     * @param messagePattern Message pattern
     */
    public final void setMessagePattern(final String messagePattern) {
        this.messagePattern = messagePattern;
    }

    /**
     * Timezone getter.
     * @return Timezone
     */
    @JsonProperty
    public final TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Timezone setter.
     * @param timeZone Timezone
     */
    @JsonProperty
    public final void setTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Check for correct parameters configuration.
     * @return is configuration valid
     */
    @JsonIgnore
    @ValidationMethod(message = "some message")
    public final boolean isValidArchiveConfiguration() {
        return true;
    }

    /**
     * Constructor.
     */
    @Override
    public Appender<E> build(final LoggerContext context, final String applicationName, final LayoutFactory<E> layoutFactory,
            final LevelFilterFactory<E> levelFilterFactory, final AsyncAppenderFactory<E> asyncAppenderFactory) {
        final SiftingAppender siftingAppender = new SiftingAppender();
        siftingAppender.setName("sift-appender");
        siftingAppender.setContext(context);

        MDCBasedDiscriminator mdcBasedDiscriminator = new MDCBasedDiscriminator();
        mdcBasedDiscriminator.setKey(discriminatorKey);
        mdcBasedDiscriminator.setDefaultValue(discriminatorDefaultValue);
        mdcBasedDiscriminator.start();
        siftingAppender.setDiscriminator(mdcBasedDiscriminator);

        siftingAppender.setAppenderFactory(new AppenderFactory<ILoggingEvent>() {

            @Override
            public Appender<ILoggingEvent> buildAppender(final Context context, final String discriminatingValue) throws JoranException {
                FileAppender<ILoggingEvent> appender = new FileAppender<>();
                appender.setAppend(true);

                appender.setName("FILE-" + discriminatingValue);
                appender.setContext(context);

                appender.setFile(Paths.get(loggingDirectory, discriminatingValue + ".log").toString());
                appender.setPrudent(false);

                PatternLayoutEncoder pl = new PatternLayoutEncoder();
                pl.setContext(context);
                pl.setPattern(messagePattern);
                pl.start();
                appender.setEncoder(pl);

                appender.start();
                return appender;
            }

        });
        siftingAppender.start();

        @SuppressWarnings("unchecked")
        Appender<E> result = (Appender<E>) siftingAppender;
        return wrapAsync(result, asyncAppenderFactory);
    }

    /**
     * Getter.
     * @return current value.
     */
    public String getLoggingDirectory() {
        return loggingDirectory;
    }

    /**
     * Setter.
     * @param loggingDirectory Directory to log to.
     */
    public void setLoggingDirectory(final String loggingDirectory) {
        this.loggingDirectory = loggingDirectory;
    }
}
