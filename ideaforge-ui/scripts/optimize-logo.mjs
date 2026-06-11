import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import sharp from 'sharp'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(__dirname, '..')

const sourceCandidates = [
  path.join(root, 'scripts', 'logo-source.png'),
  path.join(root, 'src', 'assets', 'logo.png'),
]

const sourceBackup = path.join(root, 'scripts', 'logo-source.png')
const navOutput = path.join(root, 'src', 'assets', 'logo.webp')
const faviconOutput = path.join(root, 'public', 'favicon.png')
const legacyPublicLogo = path.join(root, 'public', 'logo.png')
const legacyAssetsLogo = path.join(root, 'src', 'assets', 'logo.png')

async function findSource() {
  for (const candidate of sourceCandidates) {
    try {
      await fs.access(candidate)
      return candidate
    } catch {
      // try next candidate
    }
  }
  throw new Error(
    'Logo source not found. Place logo-source.png in scripts/ or logo.png in src/assets/.',
  )
}

async function removeIfExists(filePath) {
  try {
    await fs.unlink(filePath)
  } catch (error) {
    if (error.code !== 'ENOENT') throw error
  }
}

async function formatSize(filePath) {
  const stat = await fs.stat(filePath)
  return `${(stat.size / 1024).toFixed(1)} KB`
}

const source = await findSource()

if (source !== sourceBackup) {
  await fs.copyFile(source, sourceBackup)
  console.log(`Backed up source to ${path.relative(root, sourceBackup)}`)
}

await sharp(source)
  .resize({ width: 96, withoutEnlargement: true })
  .webp({ quality: 80 })
  .toFile(navOutput)

await sharp(source)
  .resize(48, 48, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } })
  .png({ compressionLevel: 9 })
  .toFile(faviconOutput)

await removeIfExists(legacyPublicLogo)
await removeIfExists(legacyAssetsLogo)

console.log(`Generated ${path.relative(root, navOutput)} (${await formatSize(navOutput)})`)
console.log(`Generated ${path.relative(root, faviconOutput)} (${await formatSize(faviconOutput)})`)
console.log('Removed legacy logo.png files from public/ and src/assets/.')
