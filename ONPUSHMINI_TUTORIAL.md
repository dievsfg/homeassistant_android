# `onPushMini.yml` 工作流配置教程

本文档将指导您如何配置运行 `onPushMini.yml` GitHub Actions 工作流所需的所有机密（Secrets）。请仔细按照以下步骤操作，以确保工作流能够成功编译和签名您的安卓应用。

---

## 目录

1.  [必需的机密变量列表](#1-必需的机密变量列表)
2.  [如何将文件内容进行Base64编码](#2-如何将文件内容进行base64编码)
3.  [如何生成安卓签名密钥 (Keystore)](#3-如何生成安卓签名密钥-keystore)
4.  [如何获取Firebase和Google服务文件](#4-如何获取firebase和google服务文件)
5.  [如何获取Lokalise翻译平台参数](#5-如何获取lokalise翻译平台参数)
6.  [如何获取Sentry DSN](#6-如何获取sentry-dsn)
7.  [如何将机密添加到GitHub仓库](#7-如何将机密添加到github仓库)

---

### 1. 必需的机密变量列表

以下是您需要添加到GitHub仓库`Secrets`中的所有变量：

**签名密钥相关:**
*   `ORIGINAL_KEYSTORE_FILE`: 您的签名密钥库（`.jks`文件）经过Base64编码后的内容。
*   `ORIGINAL_KEYSTORE_FILE_PASSWORD`: 密钥库的密码。
*   `ORIGINAL_KEYSTORE_ALIAS`: 密钥库中密钥的别名。
*   `ORIGINAL_KEYSTORE_ALIAS_PASSWORD`: 密钥别名的密码。

**Google & Firebase 服务:**
*   `GOOGLESERVICES`: `google-services.json`文件经过Base64编码后的内容。
*   `FIREBASECREDS`: Firebase服务账户凭证（一个`.json`文件）经过Base64编码后的内容。

**翻译服务 (当前已移除):**
*   `LOKALISE_PROJECT`: (当前不需要) Lokalise翻译平台的项目ID。
*   `LOKALISE_TOKEN`: (当前不需要) Lokalise平台的API令牌。

**崩溃报告:**
*   `SENTRY_DSN`: Sentry项目的DSN地址。

**构建缓存 (可选但推荐):**
*   `GRADLE_ENCRYPTION_KEY`: 用于加密Gradle构建缓存的密钥。**您可以将其设置为任意一个足够长且随机的字符串**。例如，您可以使用密码生成器生成一个32位的随机字符串。

---

### 2. 如何将文件内容进行Base64编码

对于`ORIGINAL_KEYSTORE_FILE`、`GOOGLESERVICES`和`FIREBASECREDS`这三个机密，您需要上传的是文件内容经过Base64编码后的字符串，而不是文件本身。

**在 Windows (使用 PowerShell):**
打开PowerShell，进入文件所在目录，然后运行以下命令（将`your_file_name`替换为您的实际文件名）：
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("your_file_name"))
```
这会输出一长串没有换行的字符串，复制它。

**在 macOS 或 Linux:**
打开终端，进入文件所在目录，然后运行以下命令：
```bash
base64 -w 0 your_file_name
```
同样，复制输出的长字符串。

---

### 3. 如何生成安卓签名密钥 (Keystore)

**重要安全提示：** **绝对不要**使用任何在线网站或第三方服务来生成您的签名密钥。签名密钥是您应用所有权的唯一证明，必须在您自己的本地计算机上生成和保管。

如果您没有安装Android Studio，可以使用Java开发工具包（JDK）中自带的`keytool`命令行工具来生成密钥。这是最简单、最安全的方法。

**方法一：使用 `keytool` 命令行 (最简单)**

1.  **打开命令行/终端**:
    *   **Windows**: 打开 `命令提示符` 或 `PowerShell`。
    *   **macOS/Linux**: 打开 `终端`。

2.  **运行命令**: 复制并粘贴以下命令，然后按回车。您可以根据需要修改文件名 (`my-release-key.jks`) 和别名 (`my-key-alias`)。

    ```bash
    keytool -genkey -v -keystore my-release-key.jks -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
    ```

3.  **回答问题**:
    *   **输入密钥库口令**: 这是您的密钥库密码 (`ORIGINAL_KEYSTORE_FILE_PASSWORD`)。输入时屏幕上不会显示，输入后按回车。
    *   **再次输入新口令**: 确认密码。
    *   **您的名字与姓氏是什么?**: 这个问题最重要，填写您的姓名或公司名。
    *   **您的组织单位名称是什么?**: 填写您的部门或团队名（可留空）。
    *   **您的组织名称是什么?**: 填写您的公司或组织名（可留空）。
    *   **您所在的城市或区域名称是什么?**: 填写城市名（可留空）。
    *   **您所在的州或省份名称是什么?**: 填写省份名（可留空）。
    *   **该单位的双字母国家代码是什么?**: 填写国家代码，例如中国的`CN`（可留空）。
    *   **确认信息**: 系统会显示您填写的信息，如果正确，输入 `yes` 并按回车。
    *   **输入密钥口令**: 这是您的密钥别名密码 (`ORIGINAL_KEYSTORE_ALIAS_PASSWORD`)。**为简单起见，强烈建议直接按回车，使其与密钥库密码相同。**

4.  **完成**: 命令执行完毕后，会在当前目录下生成一个 `my-release-key.jks` 文件。

现在，您就拥有了`.jks`文件和对应的密码/别名。请使用[第二步](#2-如何将文件内容进行base64编码)的方法对这个`.jks`文件进行编码。

---

**方法二：使用Android Studio**

如果您安装了Android Studio，也可以使用其图形化界面生成：

1.  在Android Studio菜单栏，选择 `Build` > `Generate Signed Bundle / APK...`。
2.  选择 `APK`，然后点击 `Next`。
3.  在`Key store path`字段旁边，点击`Create new...`。
4.  填写表单：
    *   **Key store path**: 选择一个安全的位置来保存您的`.jks`文件。
    *   **Password**: 创建并记住密钥库的密码 (对应`ORIGINAL_KEYSTORE_FILE_PASSWORD`)。
    *   **Alias**: 为您的密钥创建一个别名 (对应`ORIGINAL_KEYSTORE_ALIAS`)。
    *   **Password**: 创建并记住密钥别名的密码 (对应`ORIGINAL_KEYSTORE_ALIAS_PASSWORD`)。
    *   填写证书的其他信息（姓名、组织等）。
5.  点击`OK`。Android Studio会生成`.jks`文件。您**不需要**完成后续的APK生成步骤，我们只需要这个文件。

---

### 4. 如何获取Firebase和Google服务文件

1.  **访问Firebase控制台**: [https://console.firebase.google.com/](https://console.firebase.google.com/)
2.  创建一个新项目，或选择一个现有项目。
3.  **获取 `google-services.json` (重要！)**:
3.  **获取 `google-services.json` (关键步骤)**:
    *   **核心原则**: 由于您的项目有多个应用包名（`full`, `minimal`, `china`），您必须在**同一个Firebase项目**中，将这**所有三个包名**都注册为安卓应用。
    *   **操作步骤**:
        1.  在Firebase项目主页，点击安卓图标 (`</>`) 或 `Add app` 来添加第一个应用。
        2.  在“Android package name”字段中，输入`full`版本的包名: `io.homeassistant.companion.android`。
        3.  完成注册并下载 `google-services.json` 文件。
        4.  **不要停止！** 回到项目设置（点击齿轮图标 > `Project settings`）。
        5.  在 `General` 标签页下的 `Your apps` 卡片中，点击 `Add app`。
        6.  再次选择安卓图标，这次输入`minimal`版本的包名: `io.homeassistant.companion.android.minimal`。完成注册。
        7.  重复此过程，添加`china`版本的包名: `io.homeassistant.companion.android.china`。
    *   **下载最终的 `json` 文件 (包含所有应用信息)**:
        1.  **进入项目设置**: 点击Firebase控制台左上角的齿轮图标 ⚙️，选择 `Project settings`。
        2.  **检查您的应用**: 在 `General` 标签页下，向下滚动到 `Your apps` 卡片。确认您能看到所有三个已注册的应用（`...android`, `...minimal`, `...china`）。
        3.  **下载文件**: 在 `Your apps` 卡片中，找到**任意一个**安卓应用，点击其下方的 `google-services.json` 文件名链接进行下载。
        
        **重要提示**: 只要您能在这个页面看到所有应用，那么无论您从哪个应用下方下载，得到的`google-services.json`文件都是包含了**当前项目中所有应用信息**的最新版本。

        *   您可以打开这个`json`文件进行验证，在`client`数组中应该能看到三个分别对应不同`package_name`的对象。
    *   **添加SHA-1证书指纹 (可选但推荐)**:
        *   在Firebase注册应用的界面，有一个“签名证书SHA-1”的可选字段。添加这个指纹可以增强应用的安全性，并启用某些Firebase功能（如Google登录、电话号码验证等）。
        *   您可以使用`keytool`命令从您的`.jks`密钥库文件中提取这个指纹。运行以下命令（替换文件名和别名）：
            ```bash
            keytool -list -v -keystore my-release-key.jks -alias my-key-alias
            ```
        *   输入密钥库密码后，在输出的`Certificate fingerprints:`部分找到`SHA1:`的值，并将其复制到Firebase的输入框中。
    *   **为 `GOOGLESERVICES` 变量编码**:
        *   使用[第二步](#2-如何将文件内容进行base64编码)的方法，对这个**最终的、包含所有包名信息的`google-services.json`文件**进行编码。
        *   将编码后的字符串作为`GOOGLESERVICES`机密的值。这样，CI/CD工作流在构建所有变体时，都能在这个文件中找到各自的配置信息。
4.  **获取 Firebase 服务账户凭证 (`FIREBASECREDS`)**:
    *   在Firebase控制台，点击左上角的齿轮图标，选择`Project settings`。
    *   切换到`Service accounts`标签页。
    *   点击`Generate new private key`按钮，会下载一个`.json`文件。
    *   **重要提示**: 这个文件非常敏感，请妥善保管。
    *   使用[第二步](#2-如何将文件内容进行base64编码)的方法对这个新下载的`.json`文件进行编码，以获取`FIREBASECREDS`机密的值。

---

### 5. 如何获取Lokalise翻译平台参数

1.  **访问Lokalise**: [https://lokalise.com/](https://lokalise.com/)
2.  注册并创建一个项目。
3.  **获取项目ID (`LOKALISE_PROJECT`)**:
    *   进入您的项目。
    *   点击顶部菜单的`More` > `Settings`。
    *   在`General`设置页面，您会找到`Project ID`。复制它。
4.  **获取API令牌 (`LOKALISE_TOKEN`)**:
    *   点击右上角您的头像，选择`Profile settings`。
    *   切换到`API tokens`标签页。
    *   点击`Generate new token`。
    *   为令牌命名，并确保给予`Read`和`Write`权限。
    *   生成后，立即复制这个令牌。

---

### 6. 如何获取Sentry DSN

Sentry是一个用于实时监控和报告应用崩溃的平台。DSN (Data Source Name) 是一个唯一的URL，它告诉您的应用应该将崩溃报告发送到哪里。

1.  **注册并登录Sentry**:
    *   访问 [https://sentry.io/](https://sentry.io/) 并创建一个免费账户。

2.  **创建项目**:
    *   登录后，点击 `Create Project` 按钮。
    *   在平台选择页面，找到并选择 `Android`。
    *   您可以为告警设置保留默认选项，然后点击 `Create Project`。

3.  **找到您的DSN**:
    *   项目创建成功后，Sentry会向您展示一个“Configure Android SDK”的引导页面，其中包含一些命令行代码。**您不需要执行这些命令**，因为本项目已经手动完成了Sentry的集成。
    *   您只需要在这个页面或者项目设置中找到DSN即可：
        1.  进入您的项目主页。
        2.  点击左侧菜单栏的**齿轮图标** ⚙️ (`Project Settings`)。
        3.  在设置菜单中，选择 `Client Keys (DSN)`。
        4.  您会看到一个名为 `DSN` 的输入框，里面有一串URL。
        5.  点击旁边的**复制图标**，即可将完整的DSN复制到剪贴板。

这个DSN值就是您需要添加到GitHub Secrets的`SENTRY_DSN`变量的值。

---

### 7. 如何将机密添加到GitHub仓库

1.  **导航到仓库设置**: 打开您的GitHub仓库，点击顶部的`Settings`标签。
2.  **找到Actions secrets**: 在左侧菜单中，依次选择`Secrets and variables` > `Actions`。
3.  **创建新的Secret**:
    *   在`Secrets`标签页下，点击`New repository secret`按钮。
    *   在`Name`字段中输入[第一步](#1-必需的机密变量列表)中列出的变量名（例如 `ORIGINAL_KEYSTORE_FILE`）。
    *   在`Value` / `Secret`字段中粘贴您获取到的对应值（对于文件，是Base64编码后的字符串）。
    *   点击`Add secret`。
4.  **重复操作**: 为[第一步](#1-必需的机密变量列表)中列出的所有变量重复此过程。

完成以上所有步骤后，您的`onPushMini.yml`工作流就拥有了成功运行所需的所有凭证和配置。

---

### 8. 如何手动运行工作流

本工作流需要手动触发，仅仅推送代码不会自动运行。

1.  **进入Actions页面**: 在您的GitHub仓库主页，点击顶部的 `Actions` 标签。
2.  **选择工作流**: 在左侧列表中，点击 `On Push Mini`。
3.  **启动工作流**:
    *   在页面右侧，点击 `Run workflow` 按钮。
    *   在弹出的菜单中，选择您要构建代码的分支（例如 `test`）。
    *   再次点击绿色的 `Run workflow` 按钮确认。

工作流将开始运行，您可以在页面上实时查看构建进度。构建成功后，您可以在运行详情页面的“Artifacts”部分找到并下载生成的APK文件。