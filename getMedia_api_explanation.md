# SDK `getMedia` API Guide

Yeh Sakshi SDK ke `getMedia` API ke baare mein puri jankari hai ki yeh kya karta hai aur kaisa data deta hai.

## API ka Use

`getMedia` ek suspend function hai jo `SakshiClient` interface mein available hai:

```kotlin
public suspend fun getMedia(mediaType: String, fileId: String): SakshiResult<String>
```

Iska main use ek **ContentProvider URI template** lana hai. Iss URI ka use karke external apps (jaise Portal app) Vault app se specific media file access kar sakte hain.

## Parameters
Yeh API 2 parameters accept karta hai (halanki currently yeh values output ko change nahi karte, lekin API signature mein required hain):
1. `mediaType` (String): Media ka type, jaise `"PHOTO"`, `"VIDEO"`.
2. `fileId` (String): Media file ki unique ID.

## Kaam Kaise Karta Hai
1. Jab aap `getMedia` ko call karte hain, toh `SakshiClientImpl` Vault ke IPC service (AIDL) se connect karta hai.
2. Yeh Vault ke andar `ISakshiVaultService.aidl` ke through request bhejta hai.
3. Vault mein `SakshiVaultServiceBinder` iss request ko handle karta hai aur directly ek fixed template string return karta hai.

## Kya Data Deta Hai (Return Value)

Yeh method ek `SakshiResult<String>` return karta hai, jisme success hone par ek URI template milta hai.

Success condition mein yeh ek literal string return karta hai jisme `{mediaType}` aur `{fileId}` placeholders waise hi likhe hote hain (replace nahi hote).

**Exact return value structure:**
```
"content://rajnishkmehta.sakshi.vault.mediaprovider/media/{mediaType}/{fileId}"
```
*(Yahan `rajnishkmehta.sakshi.vault` Vault app ka package name hai jo device par depend ho sakta hai).*

## Zaruri Baatein
- **Memory Context Note**: Jaisa memory rules mein likha hai, *"`SakshiClient.getMedia(mediaType, fileId)` API uniquely returns a literal URI template string where `{mediaType}` and `{fileId}` remain as unreplaced string placeholders."*
- App jo is API ko consume karta hai (like Portal), use khud iss template me se `{mediaType}` aur `{fileId}` string ko replace karke final URI banani padti hai, ya seedha ContentProvider ko URI pass karni padti hai jahan Vault ka `MediaProvider` file extensions dynamically resolve karega (preventing path traversal).
