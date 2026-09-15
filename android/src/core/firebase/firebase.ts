/**
 * Firebase entry points (modular API — @react-native-firebase v22+ dropped
 * the old namespaced `firebase().collection()` compat API in favor of the
 * same modular shape as the Firebase JS SDK v9+).
 *
 * Dev builds point at the Firebase Local Emulator Suite so this app can be
 * built and manually tested without touching the live jvoice-b4b2e
 * production data. Flip USE_EMULATOR to false (or wire an env var) for a
 * real device / staging build.
 */
import { getApp } from '@react-native-firebase/app';
import { getFirestore, connectFirestoreEmulator } from '@react-native-firebase/firestore';
import { getDatabase, connectDatabaseEmulator } from '@react-native-firebase/database';
import { getAuth, connectAuthEmulator } from '@react-native-firebase/auth';
import { Platform } from 'react-native';

export const USE_EMULATOR = __DEV__;

// Android emulator maps the host machine to 10.0.2.2; iOS simulator uses localhost.
const EMULATOR_HOST = Platform.OS === 'android' ? '10.0.2.2' : 'localhost';

export const firestoreDb = getFirestore(getApp());
export const rtdb = getDatabase(getApp());
export const firebaseAuth = getAuth(getApp());

let emulatorsConnected = false;

export function connectEmulatorsIfNeeded(): void {
  if (!USE_EMULATOR || emulatorsConnected) return;
  emulatorsConnected = true;
  connectFirestoreEmulator(firestoreDb, EMULATOR_HOST, 8080);
  connectDatabaseEmulator(rtdb, EMULATOR_HOST, 9000);
  connectAuthEmulator(firebaseAuth, `http://${EMULATOR_HOST}:9099`);
}
