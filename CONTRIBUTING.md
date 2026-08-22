<h1 align="center">Contributing</h1>

<div align="center">
  <b><a href="#-development">Development</a></b> •
  <b><a href="#-building">Building</a></b> •
  <b><a href="#-reproducible-builds">Reproducible Builds</a></b>
</div>

<br>

## ![Icon](https://api.iconify.design/material-symbols/code.svg?color=%23607D8B&width=24&height=24) Development

You can quickly set up the development environment using the provided Dockerfile:

```bash
docker build --platform linux/amd64 -t stario-dev .

docker run --platform linux/amd64 --rm -it \
  -v </path/to/output>:/usr/local/stario/build \
  stario-dev
```

> [!TIP]
> Use `--rm` to automatically remove the container after use.

### Dev Container

If you use VS Code, you can open the project in a dev container for a fully configured environment (JDK 17, Android SDK, extensions) with zero manual setup:

1. Install the [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) extension.
2. Open the project folder in VS Code.
3. Click **Reopen in Container** (or use the command palette: `Dev Containers: Reopen in Container`).

The container will build automatically with all required tools and extensions pre-installed.

## ![Icon](https://api.iconify.design/material-symbols/build-outline.svg?color=%23EF6C00&width=24&height=24) Building

Should you wish to build the application yourself, run the build script from within the development environment:

```bash
# Optionally, checkout to the tagged commit
git checkout v2.9

./build.sh
```

Alternatively, to also build a signed copy (APK and AAB), pass a keystore to the build script:

```bash
docker run --platform linux/amd64 --rm -it \
  -v </path/to/output>:/usr/local/stario/build \
  -v </path/to/keystore>:/usr/local/stario/keystore \
  stario-dev

# Optionally, checkout to the tagged commit
git checkout v2.9
  
./build.sh \
  -K /usr/local/stario/keystore/keystore.jks \
  -P keystore_password \
  -a key_alias \
  -p key_password
```

## ![Icon](https://api.iconify.design/material-symbols/verified-outline.svg?color=%23009688&width=24&height=24) Reproducible Builds

Check for RBs with the locally built unsigned APK and [apksigcopier](https://github.com/obfusk/apksigcopier). 

Firstly, copy the signature from the signed APK onto your built unsigned APK:

```bash
apksigcopier copy signed-from-source.apk unsigned-built-locally.apk out.apk
```

Then compare the two APKs:

```bash
apksigcopier compare stario-from-source.apk stario-built-locally.apk
```

> [!NOTE]
> `apksigcopier compare` requires [apksigner](https://developer.android.com/tools/apksigner).
