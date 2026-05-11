FROM debian:bookworm-slim@sha256:5724d81e4ef4df1ab7042ae12310b528dba3cb5391f4200f7b07bc9a0e932b0a
WORKDIR /usr/local

# Install dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    bash \
    git \
    openjdk-17-jdk \
    wget \
    unzip \
    zip \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Setup Android SDK environment variables
ENV ANDROID_SDK_ROOT=/usr/local/android-sdk
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

# Download command line tools
RUN mkdir -p $ANDROID_SDK_ROOT/cmdline-tools && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip && \
    unzip cmdline-tools.zip -d $ANDROID_SDK_ROOT/cmdline-tools && \
    mv $ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest && \
    rm cmdline-tools.zip

# Accept licenses and install platform-tools
RUN yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses && \
    sdkmanager --sdk_root=${ANDROID_SDK_ROOT} "platform-tools"

# Create non-root user
RUN groupadd --gid 1000 builder && \
    useradd --uid 1000 --gid builder --shell /bin/bash --create-home builder

WORKDIR /usr/local/stario

# Clone the repo
COPY --chown=builder:builder . .
RUN git fetch --all

# Setup local.properties file pointing to Android SDK
RUN echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties

# Make build scripts executable
RUN chmod +x ./build.sh && \
    chmod +x ./gradlew

# Switch to non-root user for runtime
USER builder

CMD ["/bin/bash"]