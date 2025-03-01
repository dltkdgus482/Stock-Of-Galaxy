"use client";

import styled from "@emotion/styled";
import { useAccessToken } from "@/app/store/userSlice";
import { useRouter } from "next/navigation";
import React from "react";
import { FormButton } from "@/app/styles/user";
import { IBM_Plex_Sans_KR } from 'next/font/google';

const ibm = IBM_Plex_Sans_KR({ weight: '400', subsets: ['latin'] })

const StyledLoginButton = styled.input`
  color: #0e224d;
  border: none;
  border-radius: 25px;
  padding: 10px 20px;
  width: 200px;
  height: 50px;
  margin: 10px;
  font-size: 1.1rem;
  font-weight: bold;
  background-color: white;
  cursor: pointer;
  border: 1px solid #0e224d;
`;

interface MyPageButtonProps {
  value: string;
  deleteAccount?: (
    accessToken: string,
    setAccessToken: (value: string) => void
  ) => Promise<boolean>;
}

const MyPageButton: React.FC<MyPageButtonProps> = ({
  value,
  deleteAccount,
}) => {
  const router = useRouter();

  const { accessToken, setAccessToken } = useAccessToken();

  const handleDelete = async () => {
    if (deleteAccount === undefined) {
      return;
    }

    const res = await deleteAccount(accessToken, setAccessToken);

    if (res === true) {
      router.push("/login");
    }
  };

  return (
    <FormButton
    className={ibm.className}
      type="button"
      value={value}
      onClick={() => {
        value === "비밀번호 변경"
          ? router.push("/reset-password")
          : handleDelete();
      }}
    />
  );
};

export default MyPageButton;
