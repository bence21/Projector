import { User } from "../models/user";

export function currentUser(): User {
  return JSON.parse(localStorage.getItem('currentUser'));
}